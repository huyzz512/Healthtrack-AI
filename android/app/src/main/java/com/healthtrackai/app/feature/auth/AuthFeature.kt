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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthUiState(
    val identifier: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val identifierError: String? = null,
    val passwordError: String? = null,
    val statusMessage: String? = null,
)

sealed interface AuthUiEvent {
    data class IdentifierChanged(val value: String) : AuthUiEvent
    data class PasswordChanged(val value: String) : AuthUiEvent
    data object TogglePasswordVisibility : AuthUiEvent
    data object SignIn : AuthUiEvent
    data object OpenPasswordRecovery : AuthUiEvent
    data object OpenRegistration : AuthUiEvent
}

class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEvent(event: AuthUiEvent) {
        val current = _uiState.value
        _uiState.value = when (event) {
            is AuthUiEvent.IdentifierChanged -> current.copy(
                identifier = event.value,
                identifierError = null,
                statusMessage = null,
            )

            is AuthUiEvent.PasswordChanged -> current.copy(
                password = event.value,
                passwordError = null,
                statusMessage = null,
            )

            AuthUiEvent.TogglePasswordVisibility -> current.copy(
                isPasswordVisible = !current.isPasswordVisible,
            )

            AuthUiEvent.OpenPasswordRecovery -> current.copy(
                statusMessage = "Khôi phục mật khẩu sẽ khả dụng khi API xác thực được tích hợp.",
            )

            AuthUiEvent.OpenRegistration -> current.copy(
                statusMessage = "Đăng ký tài khoản sẽ khả dụng khi API xác thực được tích hợp.",
            )

            AuthUiEvent.SignIn -> {
                val identifierError = if (current.identifier.isBlank()) {
                    "Vui lòng nhập email hoặc số điện thoại."
                } else {
                    null
                }
                val passwordError = if (current.password.isBlank()) {
                    "Vui lòng nhập mật khẩu."
                } else {
                    null
                }

                current.copy(
                    identifierError = identifierError,
                    passwordError = passwordError,
                    statusMessage = if (identifierError == null && passwordError == null) {
                        "Giao diện đã sẵn sàng; API xác thực chưa được kết nối."
                    } else {
                        null
                    },
                )
            }
        }
    }
}

@Composable
fun AuthRoute(viewModel: AuthViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AuthScreen(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun AuthScreen(
    state: AuthUiState,
    onEvent: (AuthUiEvent) -> Unit,
) {
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
                text = "Chăm sóc sức khỏe\nmỗi ngày",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Đăng nhập để tiếp tục theo dõi sức khỏe của bạn.",
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
                    Text(
                        text = "Đăng nhập",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )

                    OutlinedTextField(
                        value = state.identifier,
                        onValueChange = { onEvent(AuthUiEvent.IdentifierChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email hoặc số điện thoại") },
                        placeholder = { Text("Nhập thông tin tài khoản") },
                        singleLine = true,
                        isError = state.identifierError != null,
                        supportingText = state.identifierError?.let { message ->
                            { Text(message) }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors(),
                    )

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { onEvent(AuthUiEvent.PasswordChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Mật khẩu") },
                        placeholder = { Text("Nhập mật khẩu") },
                        singleLine = true,
                        isError = state.passwordError != null,
                        supportingText = state.passwordError?.let { message ->
                            { Text(message) }
                        },
                        visualTransformation = if (state.isPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            TextButton(onClick = { onEvent(AuthUiEvent.TogglePasswordVisibility) }) {
                                Text(if (state.isPasswordVisible) "Ẩn" else "Hiện")
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors(),
                    )

                    Button(
                        onClick = { onEvent(AuthUiEvent.SignIn) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text("Đăng nhập", fontWeight = FontWeight.SemiBold)
                    }

                    state.statusMessage?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    TextButton(
                        onClick = { onEvent(AuthUiEvent.OpenPasswordRecovery) },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Text("Quên mật khẩu?")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Bạn chưa có tài khoản? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = { onEvent(AuthUiEvent.OpenRegistration) }) {
                    Text("Đăng ký", fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Bản giao diện thử nghiệm chưa gửi thông tin đăng nhập đến máy chủ.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }
    }
}

@Composable
private fun BrandMark() {
    Box(
    modifier = Modifier
            .size(60.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "+",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
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
