package com.healthtrackai.app.feature.auth

import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    @Test
    fun `password recovery requests a new recovery challenge after cooldown`() = runAuthTest {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)
        val identifier = "minh.anh@example.com"

        viewModel.onEvent(AuthUiEvent.IdentifierChanged(identifier))
        viewModel.onEvent(AuthUiEvent.SelectStage(AuthStage.FORGOT_PASSWORD))
        viewModel.onEvent(AuthUiEvent.Submit)
        advanceUntilIdle()

        assertEquals(AuthStage.RESET_PASSWORD, viewModel.uiState.value.stage)
        assertEquals(0, viewModel.uiState.value.resendsRemaining)
        viewModel.onEvent(AuthUiEvent.ResendOtp)
        advanceUntilIdle()

        assertEquals(listOf(identifier, identifier), repository.passwordResetIdentifiers)
        assertEquals(0, repository.registrationResendCount)
        assertEquals("recovery-2", viewModel.uiState.value.challengeId)
    }

    @Test
    fun `password recovery does not request another challenge before cooldown`() = runAuthTest {
        val repository = FakeAuthRepository().apply {
            recoveryResendAvailableAt = Instant.now().plusSeconds(120).toString()
        }
        val viewModel = AuthViewModel(repository)

        viewModel.onEvent(AuthUiEvent.IdentifierChanged("minh.anh@example.com"))
        viewModel.onEvent(AuthUiEvent.SelectStage(AuthStage.FORGOT_PASSWORD))
        viewModel.onEvent(AuthUiEvent.Submit)
        advanceUntilIdle()
        viewModel.onEvent(AuthUiEvent.ResendOtp)
        advanceUntilIdle()

        assertEquals(1, repository.passwordResetIdentifiers.size)
        assertEquals(0, repository.registrationResendCount)
    }

    @Test
    fun `registration resend continues using the registration challenge endpoint`() = runAuthTest {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)

        viewModel.onEvent(AuthUiEvent.SelectStage(AuthStage.REGISTER))
        viewModel.onEvent(AuthUiEvent.IdentifierChanged("minh.anh@example.com"))
        viewModel.onEvent(AuthUiEvent.FullNameChanged("Nguyễn Minh Anh"))
        viewModel.onEvent(AuthUiEvent.PasswordChanged("StrongPass1!"))
        viewModel.onEvent(AuthUiEvent.ConfirmPasswordChanged("StrongPass1!"))
        viewModel.onEvent(AuthUiEvent.Submit)
        advanceUntilIdle()
        viewModel.onEvent(AuthUiEvent.ResendOtp)
        advanceUntilIdle()

        assertEquals(AuthStage.VERIFY_REGISTRATION, viewModel.uiState.value.stage)
        assertEquals(1, repository.registrationResendCount)
        assertEquals(0, repository.passwordResetIdentifiers.size)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun runAuthTest(block: suspend TestScope.() -> Unit) = runTest {
    Dispatchers.setMain(StandardTestDispatcher(testScheduler))
    try {
        block()
    } finally {
        Dispatchers.resetMain()
    }
}

private class FakeAuthRepository : AuthRepository {
    val passwordResetIdentifiers = mutableListOf<String>()
    var registrationResendCount = 0
    var recoveryResendAvailableAt = Instant.now().minus(1, ChronoUnit.SECONDS).toString()

    override suspend fun signIn(identifier: String, password: String) = Unit

    override suspend fun register(
        fullName: String,
        identifier: String,
        password: String,
        confirmPassword: String,
    ): AuthChallengeDto = challenge("registration-1", resendsRemaining = 1)

    override suspend fun verifyRegistrationOtp(challengeId: String, code: String): Boolean = true

    override suspend fun resendOtp(challengeId: String): AuthChallengeDto {
        registrationResendCount += 1
        return challenge("registration-2", resendsRemaining = 0)
    }

    override suspend fun requestPasswordReset(identifier: String): AuthChallengeDto {
        passwordResetIdentifiers += identifier
        return challenge(
            challengeId = "recovery-${passwordResetIdentifiers.size}",
            resendsRemaining = 0,
            resendAvailableAt = recoveryResendAvailableAt,
        )
    }

    override suspend fun resetPassword(
        challengeId: String,
        code: String,
        password: String,
        confirmPassword: String,
    ) = Unit

    override suspend fun currentUser(): AuthUserDto? = null

    override suspend fun updateFullName(fullName: String): AuthUserDto =
        throw UnsupportedOperationException("Not used by this test")

    override suspend fun signOut() = Unit

    private fun challenge(
        challengeId: String,
        resendsRemaining: Int,
        resendAvailableAt: String = Instant.now().minus(1, ChronoUnit.SECONDS).toString(),
    ) = AuthChallengeDto(
        challengeId = challengeId,
        expiresAt = Instant.now().plus(5, ChronoUnit.MINUTES).toString(),
        resendAvailableAt = resendAvailableAt,
        resendsRemaining = resendsRemaining,
    )
}
