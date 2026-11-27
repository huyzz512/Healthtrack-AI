package com.healthtrackai.app.feature.auth

import com.healthtrackai.app.core.network.contract.ApiDataEnvelope
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

@Serializable
data class IdentityRequest(
    val email: String? = null,
    val phone: String? = null,
)

@Serializable
data class LoginRequest(
    val email: String? = null,
    val phone: String? = null,
    val password: String,
)

@Serializable
data class RegisterRequest(
    val email: String? = null,
    val phone: String? = null,
    val password: String,
    val fullName: String,
    val confirmPassword: String,
)

@Serializable
data class ChallengeIdRequest(val challengeId: String)

@Serializable
data class VerifyOtpRequest(val challengeId: String, val code: String)

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class LogoutRequest(val scope: String)

@Serializable
data class ResetPasswordRequest(
    val challengeId: String,
    val code: String,
    val password: String,
    val confirmPassword: String,
)

@Serializable
data class UpdateMeRequest(val fullName: String)

@Serializable
data class AuthChallengeDto(
    val challengeId: String,
    val expiresAt: String,
    val resendAvailableAt: String,
    val resendsRemaining: Int,
)

@Serializable
data class AuthSessionDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val accessTokenExpiresAt: String,
    val refreshTokenExpiresAt: String,
)

@Serializable
data class AuthUserDto(
    val id: String,
    val fullName: String,
    val email: String?,
    val phone: String?,
    val emailVerifiedAt: String?,
    val phoneVerifiedAt: String?,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class VerificationDto(val verified: Boolean)

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiDataEnvelope<AuthChallengeDto>>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<ApiDataEnvelope<VerificationDto>>

    @POST("auth/resend-otp")
    suspend fun resendOtp(@Body request: ChallengeIdRequest): Response<ApiDataEnvelope<AuthChallengeDto>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiDataEnvelope<AuthSessionDto>>

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): Response<ApiDataEnvelope<AuthSessionDto>>

    @POST("auth/logout")
    suspend fun logout(
        @Header("Authorization") authorization: String,
        @Body request: LogoutRequest,
    ): Response<Unit>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: IdentityRequest): Response<ApiDataEnvelope<AuthChallengeDto>>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<Unit>

    @GET("users/me")
    suspend fun getMe(
        @Header("Authorization") authorization: String,
    ): Response<ApiDataEnvelope<AuthUserDto>>

    @PATCH("users/me")
    suspend fun updateMe(
        @Header("Authorization") authorization: String,
        @Body request: UpdateMeRequest,
    ): Response<ApiDataEnvelope<AuthUserDto>>
}
