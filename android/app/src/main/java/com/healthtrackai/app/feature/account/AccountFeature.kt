package com.healthtrackai.app.feature.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthtrackai.app.core.di.healthTrackEntryPoint
import com.healthtrackai.app.feature.auth.AuthFailure
import com.healthtrackai.app.feature.auth.AuthRepository
import com.healthtrackai.app.feature.auth.AuthUserDto
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private val Teal = Color(0xFF0F766E)
private val Ink = Color(0xFF0F172A)
private val Muted = Color(0xFF64748B)

data class AccountUiState(
    val user: AuthUserDto? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSigningOut: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface AccountUiEvent {
    data object Load : AccountUiEvent
    data object Refresh : AccountUiEvent
    data class SaveFullName(val fullName: String) : AccountUiEvent
    data object SignOut : AccountUiEvent
}

sealed interface AccountUiEffect {
    data class SignedOut(val message: String?) : AccountUiEffect
}

class AccountViewModel(
    private val repository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()
    private val effectChannel = Channel<AccountUiEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    init {
        onEvent(AccountUiEvent.Load)
    }

    fun onEvent(event: AccountUiEvent) {
        when (event) {
            AccountUiEvent.Load, AccountUiEvent.Refresh -> load()
            is AccountUiEvent.SaveFullName -> saveFullName(event.fullName)
            AccountUiEvent.SignOut -> signOut()
        }
    }

    private fun load() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                _uiState.value = AccountUiState(user = repository.currentUser(), isLoading = false)
            } catch (failure: AuthFailure) {
                _uiState.value = AccountUiState(isLoading = false, errorMessage = failure.message)
            }
        }
    }

    private fun saveFullName(value: String) {
        val name = value.trim()
        if (name.codePointCount(0, name.length) !in 2..80) {
            _uiState.value = _uiState.value.copy(errorMessage = "Tên cần có từ 2 đến 80 ký tự.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            try {
                val user = repository.updateFullName(name)
                _uiState.value = _uiState.value.copy(user = user, isSaving = false)
            } catch (failure: AuthFailure) {
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = failure.message)
            }
        }
    }

    private fun signOut() {
        if (_uiState.value.isSigningOut) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSigningOut = true, errorMessage = null)
            try {
                repository.signOut()
                effectChannel.send(AccountUiEffect.SignedOut(null))
            } catch (failure: AuthFailure) {
                _uiState.value = AccountUiState(
                    isLoading = false,
                    errorMessage = failure.message,
                )
            }
        }
    }
}

@Composable
fun AccountRoute(
    onSignIn: () -> Unit,
    onSignedOut: (String?) -> Unit,
) {
    val context = LocalContext.current.applicationContext
    val repository = remember(context) { healthTrackEntryPoint(context).authRepository() }
    val factory = remember(repository) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass != AccountViewModel::class.java) {
                    throw IllegalArgumentException("Unsupported ViewModel type: ${modelClass.name}")
                }
                return AccountViewModel(repository) as T
            }
        }
    }
    val accountViewModel: AccountViewModel = viewModel(factory = factory)
    val state by accountViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(accountViewModel) {
        accountViewModel.effects.collect { effect ->
            when (effect) {
                is AccountUiEffect.SignedOut -> onSignedOut(effect.message)
            }
        }
    }

    AccountScreen(
        state = state,
        onEvent = accountViewModel::onEvent,
        onSignIn = onSignIn,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun AccountScreen(
    state: AccountUiState,
    onEvent: (AccountUiEvent) -> Unit,
    onSignIn: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    var editDialogVisible by remember { mutableStateOf(false) }
    var editedName by remember(state.user?.fullName) { mutableStateOf(state.user?.fullName.orEmpty()) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Tài khoản", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text("Thông tin đồng bộ từ máy chủ", fontSize = 14.sp, color = Muted)
                    }
                    TextButton(onClick = { onEvent(AccountUiEvent.Refresh) }, enabled = !state.isLoading) {
                        Text("Tải lại", color = Teal)
                    }
                }
            }
            if (state.isLoading) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                    ) {
                        Column(
                            Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CircularProgressIndicator(color = Teal)
                            Text("Đang tải thông tin tài khoản…", color = Muted)
                        }
                    }
                }
            } else if (state.user == null) {
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("Chưa đăng nhập", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Ink)
                            Text(
                                state.errorMessage ?: "Đăng nhập để tải hồ sơ tài khoản trực tiếp từ máy chủ.",
                                color = Muted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            )
                            Button(onClick = onSignIn, modifier = Modifier.fillMaxWidth()) {
                                Text("Đăng nhập")
                            }
                        }
                    }
                }
            } else {
                val user = state.user
                item {
                    AccountIdentityCard(
                        user = user,
                        onEditName = {
                            editedName = user.fullName
                            editDialogVisible = true
                        },
                    )
                }
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text("Thông tin liên hệ", fontWeight = FontWeight.Bold, color = Ink)
                            AccountValue("Email", user.email ?: "Chưa cung cấp")
                            AccountValue("Điện thoại", user.phone ?: "Chưa cung cấp")
                            AccountValue(
                                "Trạng thái email",
                                if (user.emailVerifiedAt != null) "Đã xác minh" else "Chưa xác minh",
                            )
                            AccountValue(
                                "Trạng thái điện thoại",
                                if (user.phoneVerifiedAt != null) "Đã xác minh" else "Chưa xác minh",
                            )
                        }
                    }
                }
                item {
                    Text(
                        "Thông tin cá nhân được tải từ API tài khoản. Chỉ tên hiển thị có thể cập nhật ở phiên bản này.",
                        fontSize = 12.sp,
                        color = Muted,
                        lineHeight = 18.sp,
                    )
                }
                item {
                    Button(
                        onClick = { onEvent(AccountUiEvent.SignOut) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isSigningOut,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text(if (state.isSigningOut) "Đang đăng xuất…" else "Đăng xuất")
                    }
                }
                state.errorMessage?.let { message ->
                    item { Text(message, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }

    if (editDialogVisible && state.user != null) {
        AlertDialog(
            onDismissRequest = { editDialogVisible = false },
            title = { Text("Cập nhật họ và tên") },
            text = {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Họ và tên") },
                    singleLine = true,
                    enabled = !state.isSaving,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(AccountUiEvent.SaveFullName(editedName))
                        if (editedName.trim().codePointCount(0, editedName.trim().length) in 2..80) {
                            editDialogVisible = false
                        }
                    },
                    enabled = !state.isSaving,
                ) {
                    Text(if (state.isSaving) "Đang lưu…" else "Lưu")
                }
            },
            dismissButton = {
                TextButton(onClick = { editDialogVisible = false }, enabled = !state.isSaving) {
                    Text("Hủy")
                }
            },
        )
    }
}

@Composable
private fun AccountIdentityCard(
    user: AuthUserDto,
    onEditName: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = Color(0xFFCCFBF1), modifier = Modifier.size(54.dp)) {
                androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                    Text(
                        user.fullName.firstOrNull()?.uppercase() ?: "?",
                        color = Teal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    )
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(user.fullName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("Tài khoản đã đăng nhập", fontSize = 12.sp, color = Muted)
            }
            TextButton(onClick = onEditName) {
                Text("Sửa", color = Teal)
            }
        }
    }
}

@Composable
private fun AccountValue(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = Muted, fontSize = 13.sp)
        Text(value, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
