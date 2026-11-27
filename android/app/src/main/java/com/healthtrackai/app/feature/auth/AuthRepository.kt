package com.healthtrackai.app.feature.auth

import com.healthtrackai.app.core.network.contract.ApiErrorEnvelope
import com.healthtrackai.app.core.security.EncryptedSessionStore
import com.healthtrackai.app.core.security.SessionStorageException
import com.healthtrackai.app.core.security.StoredSession
import java.io.IOException
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.Response

class AuthFailure(
    val code: String,
    val httpStatus: Int? = null,
    val traceId: String? = null,
    val challenge: AuthChallengeDto? = null,
    val retryAfterSeconds: Long? = null,
    cause: Throwable? = null,
) : Exception(authFailureMessage(code), cause)

interface AuthRepository {
    suspend fun signIn(identifier: String, password: String)
    suspend fun register(fullName: String, identifier: String, password: String, confirmPassword: String): AuthChallengeDto
    suspend fun verifyRegistrationOtp(challengeId: String, code: String): Boolean
    suspend fun resendOtp(challengeId: String): AuthChallengeDto
    suspend fun requestPasswordReset(identifier: String): AuthChallengeDto
    suspend fun resetPassword(challengeId: String, code: String, password: String, confirmPassword: String)
    suspend fun currentUser(): AuthUserDto?
    suspend fun updateFullName(fullName: String): AuthUserDto
    suspend fun signOut()
}

@Singleton
class DefaultAuthRepository @Inject constructor(
    private val api: AuthApi,
    private val sessionStore: EncryptedSessionStore,
    private val json: Json,
) : AuthRepository {
    private val refreshMutex = Mutex()

    override suspend fun signIn(identifier: String, password: String) {
        val identity = identityRequest(identifier)
        val session = requestData {
            api.login(
                LoginRequest(
                    email = identity.email,
                    phone = identity.phone,
                    password = password,
                ),
            )
        }
        saveSession(session)
    }

    override suspend fun register(
        fullName: String,
        identifier: String,
        password: String,
        confirmPassword: String,
    ): AuthChallengeDto {
        val identity = identityRequest(identifier)
        return requestData {
            api.register(
                RegisterRequest(
                    email = identity.email,
                    phone = identity.phone,
                    password = password,
                    fullName = fullName,
                    confirmPassword = confirmPassword,
                ),
            )
        }
    }

    override suspend fun verifyRegistrationOtp(challengeId: String, code: String): Boolean =
        requestData { api.verifyOtp(VerifyOtpRequest(challengeId, code)) }.verified

    override suspend fun resendOtp(challengeId: String): AuthChallengeDto =
        requestData { api.resendOtp(ChallengeIdRequest(challengeId)) }

    override suspend fun requestPasswordReset(identifier: String): AuthChallengeDto =
        requestData { api.forgotPassword(identityRequest(identifier)) }

    override suspend fun resetPassword(
        challengeId: String,
        code: String,
        password: String,
        confirmPassword: String,
    ) {
        requestUnit {
            api.resetPassword(
                ResetPasswordRequest(
                    challengeId = challengeId,
                    code = code,
                    password = password,
                    confirmPassword = confirmPassword,
                ),
            )
        }
    }

    override suspend fun currentUser(): AuthUserDto? {
        val storedSession = readStoredSession() ?: return null
        val activeSession = usableSession(storedSession)
        return try {
            requestData { api.getMe(bearer(activeSession.accessToken)) }
        } catch (failure: AuthFailure) {
            if (failure.code !in TOKEN_FAILURES) throw failure
            val refreshed = refreshSession(activeSession)
            requestData { api.getMe(bearer(refreshed.accessToken)) }
        }
    }

    override suspend fun updateFullName(fullName: String): AuthUserDto {
        val activeSession = usableSession(readStoredSession() ?: throw AuthFailure("AUTH_REQUIRED"))
        return try {
            requestData { api.updateMe(bearer(activeSession.accessToken), UpdateMeRequest(fullName)) }
        } catch (failure: AuthFailure) {
            if (failure.code !in TOKEN_FAILURES) throw failure
            val refreshed = refreshSession(activeSession)
            requestData { api.updateMe(bearer(refreshed.accessToken), UpdateMeRequest(fullName)) }
        }
    }

    override suspend fun signOut() {
        val storedSession = readStoredSession() ?: return
        var apiFailure: AuthFailure? = null
        try {
            val activeSession = usableSession(storedSession)
            requestUnit { api.logout(bearer(activeSession.accessToken), LogoutRequest("CURRENT")) }
        } catch (failure: AuthFailure) {
            apiFailure = failure
        } finally {
            clearStoredSession()
        }
        apiFailure?.let { throw it }
    }

    private suspend fun usableSession(session: StoredSession): StoredSession {
        val accessExpiry = parseExpiry(session.accessTokenExpiresAt)
        return if (accessExpiry.isAfter(Instant.now().plusSeconds(30))) {
            session
        } else {
            refreshSession(session)
        }
    }

    private suspend fun refreshSession(previous: StoredSession): StoredSession =
        refreshMutex.withLock {
            val latest = readStoredSession() ?: throw AuthFailure("AUTH_REQUIRED")
            if (latest.accessToken != previous.accessToken &&
                parseExpiry(latest.accessTokenExpiresAt).isAfter(Instant.now().plusSeconds(30))
            ) {
                return@withLock latest
            }
            if (!parseExpiry(latest.refreshTokenExpiresAt).isAfter(Instant.now())) {
                clearStoredSession()
                throw AuthFailure("AUTH_REFRESH_INVALID", httpStatus = 401)
            }

            val refreshed = try {
                requestData { api.refresh(RefreshRequest(latest.refreshToken)) }
            } catch (failure: AuthFailure) {
                if (failure.httpStatus == 401 || failure.code == "AUTH_REFRESH_INVALID") {
                    clearStoredSession()
                }
                throw failure
            }
            saveSession(refreshed)
            readStoredSession() ?: throw AuthFailure("SESSION_STORAGE_ERROR")
        }

    private suspend fun saveSession(session: AuthSessionDto) {
        if (session.tokenType != "Bearer") throw AuthFailure("SERVER_RESPONSE_INVALID")
        try {
            sessionStore.write(
                StoredSession(
                    accessToken = session.accessToken,
                    refreshToken = session.refreshToken,
                    accessTokenExpiresAt = session.accessTokenExpiresAt,
                    refreshTokenExpiresAt = session.refreshTokenExpiresAt,
                ),
            )
        } catch (failure: SessionStorageException) {
            throw AuthFailure("SESSION_STORAGE_ERROR", cause = failure)
        }
    }

    private suspend fun readStoredSession(): StoredSession? = try {
        sessionStore.read()
    } catch (failure: SessionStorageException) {
        throw AuthFailure("SESSION_STORAGE_ERROR", cause = failure)
    }

    private suspend fun clearStoredSession() {
        try {
            sessionStore.clear()
        } catch (failure: SessionStorageException) {
            throw AuthFailure("SESSION_STORAGE_ERROR", cause = failure)
        }
    }

    private fun parseExpiry(value: String): Instant = try {
        Instant.parse(value)
    } catch (failure: Exception) {
        throw AuthFailure("SERVER_RESPONSE_INVALID", cause = failure)
    }

    private fun identityRequest(identifier: String): IdentityRequest {
        val value = identifier.trim()
        if (value.isBlank()) throw AuthFailure("VALIDATION_FAILED")
        return if (value.contains('@')) {
            IdentityRequest(email = value.lowercase())
        } else {
            IdentityRequest(phone = value)
        }
    }

    private fun bearer(accessToken: String) = "Bearer $accessToken"

    private suspend fun <T> requestData(
        request: suspend () -> Response<com.healthtrackai.app.core.network.contract.ApiDataEnvelope<T>>,
    ): T = execute { responseData(request()) }

    private suspend fun requestUnit(request: suspend () -> Response<Unit>) {
        execute { responseUnit(request()) }
    }

    private suspend fun <T> execute(action: suspend () -> T): T = try {
        action()
    } catch (failure: IOException) {
        throw AuthFailure("NETWORK_UNAVAILABLE", cause = failure)
    } catch (failure: SerializationException) {
        throw AuthFailure("SERVER_RESPONSE_INVALID", cause = failure)
    }

    private fun <T> responseData(response: Response<com.healthtrackai.app.core.network.contract.ApiDataEnvelope<T>>): T {
        if (!response.isSuccessful) throw parseFailure(response.code(), response.errorBody()?.string())
        return response.body()?.data ?: throw AuthFailure("SERVER_RESPONSE_INVALID", httpStatus = response.code())
    }

    private fun responseUnit(response: Response<Unit>) {
        if (!response.isSuccessful) throw parseFailure(response.code(), response.errorBody()?.string())
    }

    private fun parseFailure(status: Int, body: String?): AuthFailure {
        val error = if (body.isNullOrBlank()) {
            null
        } else {
            try {
                json.decodeFromString<ApiErrorEnvelope>(body).error
            } catch (_: SerializationException) {
                null
            }
        }
        val details = error?.details as? JsonObject
        val challenge = if (error?.code == "AUTH_ACCOUNT_UNVERIFIED" && details != null) {
            try {
                json.decodeFromJsonElement<AuthChallengeDto>(details)
            } catch (_: SerializationException) {
                null
            }
        } else {
            null
        }
        val retryAfter = details?.get("retryAfterSeconds")
            ?.let { value -> runCatching { value.jsonPrimitive.content.toLong() }.getOrNull() }
        return AuthFailure(
            code = error?.code ?: when (status) {
                401 -> "AUTH_TOKEN_INVALID"
                429 -> "AUTH_RATE_LIMITED"
                in 500..599 -> "SERVER_UNAVAILABLE"
                else -> "REQUEST_REJECTED"
            },
            httpStatus = status,
            traceId = error?.traceId,
            challenge = challenge,
            retryAfterSeconds = retryAfter,
        )
    }

    private companion object {
        val TOKEN_FAILURES = setOf("AUTH_TOKEN_EXPIRED", "AUTH_TOKEN_INVALID")
    }
}

private fun authFailureMessage(code: String): String = when (code) {
    "AUTH_INVALID_CREDENTIALS" -> "Email/số điện thoại hoặc mật khẩu không chính xác."
    "AUTH_IDENTITY_UNAVAILABLE" -> "Thông tin này đã được đăng ký. Hãy đăng nhập hoặc dùng thông tin khác."
    "AUTH_ACCOUNT_UNVERIFIED" -> "Tài khoản chưa được xác minh. Hãy nhập mã OTP đã nhận."
    "AUTH_ACCOUNT_LOCKED" -> "Tài khoản đang tạm khóa. Vui lòng thử lại sau."
    "AUTH_ACCOUNT_DISABLED" -> "Tài khoản hiện không hoạt động. Vui lòng liên hệ hỗ trợ."
    "AUTH_OTP_INVALID" -> "Mã OTP không đúng hoặc đã được sử dụng."
    "AUTH_OTP_EXPIRED" -> "Mã OTP đã hết hạn. Hãy yêu cầu gửi lại mã."
    "AUTH_OTP_ATTEMPTS_EXCEEDED" -> "Bạn đã nhập sai OTP quá số lần cho phép."
    "AUTH_OTP_RESEND_LIMIT" -> "Bạn đã yêu cầu gửi lại OTP quá số lần cho phép."
    "AUTH_OTP_UNAVAILABLE" -> "Dịch vụ gửi OTP hiện không khả dụng. Vui lòng thử lại sau."
    "AUTH_RATE_LIMITED" -> "Bạn thao tác quá nhanh. Vui lòng đợi rồi thử lại."
    "AUTH_REFRESH_INVALID", "AUTH_TOKEN_INVALID", "AUTH_TOKEN_EXPIRED", "AUTH_REQUIRED" ->
        "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
    "AUTH_CONFIGURATION_UNAVAILABLE" -> "Dịch vụ xác thực chưa được cấu hình đầy đủ."
    "VALIDATION_FAILED" -> "Thông tin gửi lên chưa hợp lệ. Hãy kiểm tra lại các trường."
    "DATABASE_UNAVAILABLE", "SERVER_UNAVAILABLE" -> "Máy chủ tạm thời không khả dụng. Vui lòng thử lại."
    "NETWORK_UNAVAILABLE" -> "Không thể kết nối máy chủ. Hãy kiểm tra mạng và địa chỉ API."
    "SESSION_STORAGE_ERROR" -> "Không thể lưu phiên đăng nhập an toàn trên thiết bị."
    "SERVER_RESPONSE_INVALID" -> "Máy chủ trả về dữ liệu không hợp lệ."
    "REQUEST_REJECTED" -> "Máy chủ từ chối yêu cầu. Vui lòng kiểm tra thông tin."
    else -> "Đã xảy ra lỗi khi kết nối máy chủ. Vui lòng thử lại."
}
