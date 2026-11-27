package com.healthtrackai.app.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthtrackai.app.core.di.healthTrackEntryPoint
import java.time.Instant
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private val Teal = Color(0xFF0F766E)
private val Ink = Color(0xFF0F172A)
private val Muted = Color(0xFF64748B)

enum class AuthStage {
    SIGN_IN,
    REGISTER,
    VERIFY_REGISTRATION,
    FORGOT_PASSWORD,
    RESET_PASSWORD,
}

data class AuthUiState(
    val stage: AuthStage = AuthStage.SIGN_IN,
    val identifier: String = "",
    val fullName: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val otpCode: String = "",
    val challengeId: String? = null,
    val resendAvailableAt: String? = null,
    val resendsRemaining: Int = 0,
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val statusMessage: String? = null,
)

sealed interface AuthUiEvent {
    data class IdentifierChanged(val value: String) : AuthUiEvent
    data class FullNameChanged(val value: String) : AuthUiEvent
    data class PasswordChanged(val value: String) : AuthUiEvent
    data class ConfirmPasswordChanged(val value: String) : AuthUiEvent
    data class OtpChanged(val value: String) : AuthUiEvent
    data class SelectStage(val stage: AuthStage) : AuthUiEvent
    data object TogglePasswordVisibility : AuthUiEvent
    data object Submit : AuthUiEvent
    data object ResendOtp : AuthUiEvent
}

sealed interface AuthUiEffect {
    data object Authenticated : AuthUiEffect
}

class AuthViewModel(
    private val repository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    private val effectChannel = Channel<AuthUiEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    fun onEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.IdentifierChanged -> update { copy(identifier = event.value, errorMessage = null) }
            is AuthUiEvent.FullNameChanged -> update { copy(fullName = event.value, errorMessage = null) }
            is AuthUiEvent.PasswordChanged -> update { copy(password = event.value, errorMessage = null) }
            is AuthUiEvent.ConfirmPasswordChanged -> update { copy(confirmPassword = event.value, errorMessage = null) }
            is AuthUiEvent.OtpChanged -> update { copy(otpCode = event.value.filter(Char::isDigit).take(6), errorMessage = null) }
            is AuthUiEvent.SelectStage -> update {
                copy(
                    stage = event.stage,
                    password = "",
                    confirmPassword = "",
                    otpCode = "",
                    errorMessage = null,
                    statusMessage = null,
                )
            }
            AuthUiEvent.TogglePasswordVisibility -> update { copy(passwordVisible = !passwordVisible) }
            AuthUiEvent.Submit -> submit()
            AuthUiEvent.ResendOtp -> resendOtp()
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (state.isLoading) return
        when (state.stage) {
            AuthStage.SIGN_IN -> signIn(state)
            AuthStage.REGISTER -> register(state)
            AuthStage.VERIFY_REGISTRATION -> verifyRegistration(state)
            AuthStage.FORGOT_PASSWORD -> requestPasswordReset(state)
            AuthStage.RESET_PASSWORD -> resetPassword(state)
        }
    }

    private fun signIn(state: AuthUiState) {
        val validation = validateIdentityAndPassword(state.identifier, state.password)
        if (validation != null) {
            update { copy(errorMessage = validation) }
            return
        }
        performRequest {
            repository.signIn(state.identifier, state.password)
            effectChannel.send(AuthUiEffect.Authenticated)
        }
    }

    private fun register(state: AuthUiState) {
        val name = state.fullName.trim()
        if (name.codePointCount(0, name.length) !in 2..80) {
            update { copy(errorMessage = "Tên cần có từ 2 đến 80 ký tự.") }
            return
        }
        val validation = validateIdentityAndPassword(state.identifier, state.password)
        if (validation != null) {
            update { copy(errorMessage = validation) }
            return
        }
        if (state.confirmPassword != state.password) {
            update { copy(errorMessage = "Mật khẩu xác nhận chưa khớp.") }
            return
        }
        if (!isStrongPassword(state.password)) {
            update { copy(errorMessage = "Mật khẩu cần 8–30 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt.") }
            return
        }
        performRequest {
            val challenge = repository.register(name, state.identifier, state.password, state.confirmPassword)
            update {
                copy(
                    stage = AuthStage.VERIFY_REGISTRATION,
                    challengeId = challenge.challengeId,
                    resendAvailableAt = challenge.resendAvailableAt,
                    resendsRemaining = challenge.resendsRemaining,
                    otpCode = "",
                    password = "",
                    confirmPassword = "",
                    statusMessage = "Đã gửi yêu cầu xác minh. Hãy nhập mã OTP do hệ thống gửi đến bạn.",
                )
            }
        }
    }

    private fun verifyRegistration(state: AuthUiState) {
        val challengeId = state.challengeId
        if (challengeId.isNullOrBlank()) {
            update { copy(errorMessage = "Không tìm thấy phiên xác minh. Hãy bắt đầu đăng ký lại.") }
            return
        }
        if (!OTP_PATTERN.matches(state.otpCode)) {
            update { copy(errorMessage = "Mã xác minh cần gồm đúng 6 chữ số.") }
            return
        }
        performRequest {
            if (!repository.verifyRegistrationOtp(challengeId, state.otpCode)) {
                throw AuthFailure("SERVER_RESPONSE_INVALID")
            }
            update {
                copy(
                    stage = AuthStage.SIGN_IN,
                    challengeId = null,
                    otpCode = "",
                    password = "",
                    statusMessage = "Xác minh thành công. Hãy đăng nhập bằng tài khoản vừa tạo.",
                )
            }
        }
    }

    private fun requestPasswordReset(state: AuthUiState) {
        if (state.identifier.isBlank()) {
            update { copy(errorMessage = "Vui lòng nhập email hoặc số điện thoại.") }
            return
        }
        performRequest {
            val challenge = repository.requestPasswordReset(state.identifier)
            update {
                copy(
                    stage = AuthStage.RESET_PASSWORD,
                    challengeId = challenge.challengeId,
                    resendAvailableAt = challenge.resendAvailableAt,
                    resendsRemaining = challenge.resendsRemaining,
                    otpCode = "",
                    password = "",
                    confirmPassword = "",
                    statusMessage = "Nếu thông tin hợp lệ, mã xác minh sẽ được gửi. Hãy nhập mã để tiếp tục.",
                )
            }
        }
    }

    private fun resetPassword(state: AuthUiState) {
        val challengeId = state.challengeId
        if (challengeId.isNullOrBlank()) {
            update { copy(errorMessage = "Không tìm thấy yêu cầu khôi phục. Hãy bắt đầu lại.") }
            return
        }
        if (!OTP_PATTERN.matches(state.otpCode)) {
            update { copy(errorMessage = "Mã xác minh cần gồm đúng 6 chữ số.") }
            return
        }
        if (!isStrongPassword(state.password)) {
            update { copy(errorMessage = "Mật khẩu cần 8–30 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt.") }
            return
        }
        if (state.password != state.confirmPassword) {
            update { copy(errorMessage = "Mật khẩu xác nhận chưa khớp.") }
            return
        }
        performRequest {
            repository.resetPassword(challengeId, state.otpCode, state.password, state.confirmPassword)
            update {
                copy(
                    stage = AuthStage.SIGN_IN,
                    challengeId = null,
                    otpCode = "",
                    password = "",
                    confirmPassword = "",
                    statusMessage = "Đã đổi mật khẩu. Hãy đăng nhập bằng mật khẩu mới.",
                )
            }
        }
    }

    private fun resendOtp() {
        val state = _uiState.value
        val challengeId = state.challengeId
        if (
            state.isLoading ||
            state.stage !in setOf(AuthStage.VERIFY_REGISTRATION, AuthStage.RESET_PASSWORD) ||
            challengeId.isNullOrBlank()
        ) return
        if (state.resendsRemaining <= 0) {
            update { copy(errorMessage = "Bạn đã yêu cầu gửi lại OTP quá số lần cho phép.") }
            return
        }
        performRequest {
            val challenge = repository.resendOtp(challengeId)
            update {
                copy(
                    challengeId = challenge.challengeId,
                    resendAvailableAt = challenge.resendAvailableAt,
                    resendsRemaining = challenge.resendsRemaining,
                    otpCode = "",
                    statusMessage = "Đã gửi yêu cầu gửi lại mã OTP.",
                )
            }
        }
    }

    private fun performRequest(request: suspend () -> Unit) {
        viewModelScope.launch {
            update { copy(isLoading = true, errorMessage = null) }
            try {
                request()
            } catch (failure: AuthFailure) {
                val challenge = failure.challenge
                update {
                    if (failure.code == "AUTH_ACCOUNT_UNVERIFIED" && challenge != null) {
                        copy(
                            stage = AuthStage.VERIFY_REGISTRATION,
                            challengeId = challenge.challengeId,
                            resendAvailableAt = challenge.resendAvailableAt,
                            resendsRemaining = challenge.resendsRemaining,
                            password = "",
                            errorMessage = failure.message,
                        )
                    } else {
                        copy(errorMessage = failure.message)
                    }
                }
            } finally {
                update { copy(isLoading = false) }
            }
        }
    }

    private fun update(transform: AuthUiState.() -> AuthUiState) {
        _uiState.value = _uiState.value.transform()
    }

    private fun validateIdentityAndPassword(identifier: String, password: String): String? {
        if (identifier.isBlank()) return "Vui lòng nhập email hoặc số điện thoại."
        if (password.isBlank()) return "Vui lòng nhập mật khẩu."
        return null
    }

    private fun isStrongPassword(password: String): Boolean =
        password.codePointCount(0, password.length) in 8..30 &&
            password.any { it in 'A'..'Z' } &&
            password.any { it in 'a'..'z' } &&
            password.any { it in '0'..'9' } &&
            password.any { character ->
                character !in 'A'..'Z' &&
                    character !in 'a'..'z' &&
                    character !in '0'..'9' &&
                    !character.isWhitespace()
            }

    private companion object {
        val OTP_PATTERN = Regex("^[0-9]{6}$")
    }
}

@Composable
fun AuthRoute(
    onAuthenticated: () -> Unit,
) {
    val context = LocalContext.current.applicationContext
    val repository = remember(context) { healthTrackEntryPoint(context).authRepository() }
    val factory = remember(repository) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass != AuthViewModel::class.java) {
                    throw IllegalArgumentException("Unsupported ViewModel type: ${modelClass.name}")
                }
                return AuthViewModel(repository) as T
            }
        }
    }
    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val state by authViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(authViewModel, onAuthenticated) {
        authViewModel.effects.collect { effect ->
            when (effect) {
                AuthUiEffect.Authenticated -> onAuthenticated()
            }
        }
    }
    AuthScreen(state = state, onEvent = authViewModel::onEvent)
}

@Composable
fun AuthScreen(
    state: AuthUiState,
    onEvent: (AuthUiEvent) -> Unit,
) {
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.resendAvailableAt, state.stage) {
        while (
            state.stage in setOf(AuthStage.VERIFY_REGISTRATION, AuthStage.RESET_PASSWORD) &&
            state.resendAvailableAt != null
        ) {
            currentTimeMillis = System.currentTimeMillis()
            delay(1_000)
        }
    }

    val title = when (state.stage) {
        AuthStage.SIGN_IN -> "Đăng nhập"
        AuthStage.REGISTER -> "Tạo tài khoản"
        AuthStage.VERIFY_REGISTRATION -> "Xác minh tài khoản"
        AuthStage.FORGOT_PASSWORD -> "Khôi phục mật khẩu"
        AuthStage.RESET_PASSWORD -> "Đặt mật khẩu mới"
    }
    val resendWait = state.resendAvailableAt
        ?.let { value -> runCatching { (Instant.parse(value).toEpochMilli() - currentTimeMillis).coerceAtLeast(0) / 1_000 }.getOrNull() }
        ?: 0L

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            BrandMark()
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "HealthTrack AI",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = when (state.stage) {
                    AuthStage.SIGN_IN -> "Đăng nhập bằng tài khoản HealthTrack AI của bạn."
                    AuthStage.REGISTER -> "Tạo tài khoản để bắt đầu sử dụng HealthTrack AI."
                    AuthStage.VERIFY_REGISTRATION -> "Nhập mã 6 chữ số đã được gửi đến thông tin đăng ký."
                    AuthStage.FORGOT_PASSWORD -> "Nhập email hoặc số điện thoại để yêu cầu mã khôi phục."
                    AuthStage.RESET_PASSWORD -> "Nhập mã xác minh và mật khẩu mới của bạn."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(18.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)

                    if (state.stage == AuthStage.REGISTER) {
                        AuthTextField(
                            value = state.fullName,
                            onChange = { onEvent(AuthUiEvent.FullNameChanged(it)) },
                            label = "Họ và tên",
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                            enabled = !state.isLoading,
                        )
                    }
                    if (state.stage in setOf(AuthStage.SIGN_IN, AuthStage.REGISTER, AuthStage.FORGOT_PASSWORD)) {
                        AuthTextField(
                            value = state.identifier,
                            onChange = { onEvent(AuthUiEvent.IdentifierChanged(it)) },
                            label = "Email hoặc số điện thoại",
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                            enabled = !state.isLoading,
                        )
                    }
                    if (state.stage in setOf(AuthStage.SIGN_IN, AuthStage.REGISTER, AuthStage.RESET_PASSWORD)) {
                        AuthTextField(
                            value = state.password,
                            onChange = { onEvent(AuthUiEvent.PasswordChanged(it)) },
                            label = if (state.stage == AuthStage.RESET_PASSWORD) "Mật khẩu mới" else "Mật khẩu",
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next,
                            enabled = !state.isLoading,
                            isPassword = true,
                            visible = state.passwordVisible,
                            onToggleVisibility = { onEvent(AuthUiEvent.TogglePasswordVisibility) },
                        )
                    }
                    if (state.stage in setOf(AuthStage.REGISTER, AuthStage.RESET_PASSWORD)) {
                        AuthTextField(
                            value = state.confirmPassword,
                            onChange = { onEvent(AuthUiEvent.ConfirmPasswordChanged(it)) },
                            label = "Xác nhận mật khẩu",
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                            enabled = !state.isLoading,
                            isPassword = true,
                            visible = state.passwordVisible,
                            onToggleVisibility = { onEvent(AuthUiEvent.TogglePasswordVisibility) },
                        )
                    }
                    if (state.stage in setOf(AuthStage.VERIFY_REGISTRATION, AuthStage.RESET_PASSWORD)) {
                        AuthTextField(
                            value = state.otpCode,
                            onChange = { onEvent(AuthUiEvent.OtpChanged(it)) },
                            label = "Mã xác minh gồm 6 chữ số",
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                            enabled = !state.isLoading,
                        )
                    }

                    state.errorMessage?.let { message ->
                        Text(
                            message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    state.statusMessage?.let { message ->
                        Text(
                            message,
                            color = Teal,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    if (
                        state.stage in setOf(AuthStage.VERIFY_REGISTRATION, AuthStage.RESET_PASSWORD) &&
                        state.resendsRemaining > 0
                    ) {
                        TextButton(
                            onClick = { onEvent(AuthUiEvent.ResendOtp) },
                            enabled = !state.isLoading && resendWait == 0L,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        ) {
                            Text(
                                if (resendWait > 0) "Gửi lại mã sau ${resendWait}s" else "Gửi lại mã OTP",
                            )
                        }
                    }
                    Button(
                        onClick = { onEvent(AuthUiEvent.Submit) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Teal),
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(primaryAction(state.stage), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            when (state.stage) {
                AuthStage.SIGN_IN -> {
                    TextButton(onClick = { onEvent(AuthUiEvent.SelectStage(AuthStage.FORGOT_PASSWORD)) }) {
                        Text("Quên mật khẩu?")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Bạn chưa có tài khoản? ", color = Muted)
                        TextButton(onClick = { onEvent(AuthUiEvent.SelectStage(AuthStage.REGISTER)) }) {
                            Text("Đăng ký", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                AuthStage.REGISTER,
                AuthStage.FORGOT_PASSWORD,
                AuthStage.VERIFY_REGISTRATION,
                AuthStage.RESET_PASSWORD -> {
                    TextButton(
                        onClick = {
                            val target = if (state.stage == AuthStage.REGISTER || state.stage == AuthStage.VERIFY_REGISTRATION) {
                                AuthStage.SIGN_IN
                            } else {
                                AuthStage.FORGOT_PASSWORD
                            }
                            onEvent(AuthUiEvent.SelectStage(target))
                        },
                    ) { Text("Quay lại đăng nhập") }
                }
            }
            Text(
                text = "Kết nối trực tiếp với API HealthTrack AI.",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    enabled: Boolean,
    isPassword: Boolean = false,
    visible: Boolean = false,
    onToggleVisibility: (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        visualTransformation = if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (isPassword && onToggleVisibility != null) {
            {
                TextButton(onClick = onToggleVisibility) {
                    Text(if (visible) "Ẩn" else "Hiện")
                }
            }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        shape = RoundedCornerShape(14.dp),
        colors = fieldColors(),
    )
}

private fun primaryAction(stage: AuthStage): String = when (stage) {
    AuthStage.SIGN_IN -> "Đăng nhập"
    AuthStage.REGISTER -> "Tạo tài khoản"
    AuthStage.VERIFY_REGISTRATION -> "Xác minh tài khoản"
    AuthStage.FORGOT_PASSWORD -> "Gửi mã khôi phục"
    AuthStage.RESET_PASSWORD -> "Đặt lại mật khẩu"
}

@Composable
private fun BrandMark() {
    Box(
        modifier = Modifier.size(60.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "HT",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    cursorColor = MaterialTheme.colorScheme.primary,
    errorBorderColor = MaterialTheme.colorScheme.error,
    errorLabelColor = MaterialTheme.colorScheme.error,
)
