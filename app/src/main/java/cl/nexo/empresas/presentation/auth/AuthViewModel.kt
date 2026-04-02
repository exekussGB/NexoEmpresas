package cl.nexo.empresas.presentation.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.nexo.empresas.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    val isLoggedIn = mutableStateOf<Boolean?>(null)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _forgotState = MutableStateFlow<AuthUiState>(AuthUiState.ForgotPasswordIdle)
    val forgotState: StateFlow<AuthUiState> = _forgotState

    private val _otpState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val otpState: StateFlow<AuthUiState> = _otpState

    private val _resetPasswordState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val resetPasswordState: StateFlow<AuthUiState> = _resetPasswordState

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            try {
                val status = supabaseClient.auth.sessionStatus.first { it !is SessionStatus.Initializing }
                isLoggedIn.value = status is SessionStatus.Authenticated
            } catch (e: Exception) {
                isLoggedIn.value = false
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.login(email, password)
                .onSuccess {
                    isLoggedIn.value = true
                    _uiState.value = AuthUiState.Success
                }
                .onFailure { e -> _uiState.value = AuthUiState.Error(parseLoginError(e)) }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.register(email, password)
                .onSuccess {
                    authRepository.login(email, password)
                        .onSuccess {
                            isLoggedIn.value = true
                            _uiState.value = AuthUiState.Success
                        }
                        .onFailure { _uiState.value = AuthUiState.EmailPendingConfirmation }
                }
                .onFailure { e -> _uiState.value = AuthUiState.Error(parseRegisterError(e)) }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _forgotState.value = AuthUiState.Loading
            authRepository.resetPassword(email)
                .onSuccess {
                    _forgotState.value = AuthUiState.ResetEmailSent
                }
                .onFailure { e ->
                    _forgotState.value = AuthUiState.Error(parseResetError(e))
                }
        }
    }

    fun verifyOtp(email: String, code: String) {
        viewModelScope.launch {
            _otpState.value = AuthUiState.Loading
            authRepository.verifyRecoveryOtp(email, code)
                .onSuccess {
                    _otpState.value = AuthUiState.OtpVerified
                }
                .onFailure { e ->
                    _otpState.value = AuthUiState.Error(parseOtpError(e))
                }
        }
    }

    fun resetPasswordWithNew(newPassword: String) {
        viewModelScope.launch {
            _resetPasswordState.value = AuthUiState.Loading
            authRepository.updatePassword(newPassword)
                .onSuccess {
                    _resetPasswordState.value = AuthUiState.PasswordResetSuccess
                }
                .onFailure { e ->
                    _resetPasswordState.value = AuthUiState.Error(parseUpdatePasswordError(e))
                }
        }
    }

    fun resetForgotState() {
        _forgotState.value = AuthUiState.ForgotPasswordIdle
    }

    fun resetOtpState() {
        _otpState.value = AuthUiState.Idle
    }

    fun resetResetPasswordState() {
        _resetPasswordState.value = AuthUiState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            isLoggedIn.value = false
        }
    }

    private fun parseLoginError(e: Throwable): String = when {
        e.message?.contains("email_not_confirmed", ignoreCase = true) == true ->
            "Email no confirmado. Revisa tu bandeja de entrada."
        e.message?.contains("Invalid login credentials", ignoreCase = true) == true ->
            "Email o contraseña incorrectos."
        e.message?.contains("rate limit", ignoreCase = true) == true ||
        e.message?.contains("over_email_send_rate_limit", ignoreCase = true) == true ->
            "Demasiados intentos. Espera unos minutos e intenta nuevamente."
        e.message?.contains("network", ignoreCase = true) == true ||
        e.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
            "Sin conexión a internet. Verifica tu red."
        else -> "Error al iniciar sesión. Intenta nuevamente."
    }

    private fun parseRegisterError(e: Throwable): String = when {
        e.message?.contains("User already registered", ignoreCase = true) == true ->
            "Ya existe una cuenta con este email."
        e.message?.contains("Password should be at least", ignoreCase = true) == true ->
            "La contraseña debe tener al menos 6 caracteres."
        e.message?.contains("Unable to validate email", ignoreCase = true) == true ||
        e.message?.contains("invalid email", ignoreCase = true) == true ->
            "El formato del email no es válido."
        e.message?.contains("rate limit", ignoreCase = true) == true ->
            "Demasiados intentos. Espera unos minutos e intenta nuevamente."
        e.message?.contains("network", ignoreCase = true) == true ||
        e.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
            "Sin conexión a internet. Verifica tu red."
        else -> "Error al registrarse. Intenta nuevamente."
    }

    private fun parseResetError(e: Throwable): String = when {
        e.message?.contains("rate limit", ignoreCase = true) == true ||
        e.message?.contains("over_email_send_rate_limit", ignoreCase = true) == true ->
            "Demasiados intentos. Espera unos minutos e intenta nuevamente."
        e.message?.contains("network", ignoreCase = true) == true ||
        e.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
            "Sin conexión a internet. Verifica tu red."
        e.message?.contains("Unable to validate email", ignoreCase = true) == true ||
        e.message?.contains("invalid email", ignoreCase = true) == true ->
            "El formato del email no es válido."
        else -> "Error al enviar el correo de recuperación. Intenta nuevamente."
    }

    private fun parseOtpError(e: Throwable): String = when {
        e.message?.contains("otp_expired", ignoreCase = true) == true ||
        e.message?.contains("expired", ignoreCase = true) == true ->
            "El código ha expirado. Solicita uno nuevo."
        e.message?.contains("otp_disabled", ignoreCase = true) == true ->
            "La verificación por código no está habilitada."
        e.message?.contains("rate limit", ignoreCase = true) == true ->
            "Demasiados intentos. Espera unos minutos e intenta nuevamente."
        e.message?.contains("network", ignoreCase = true) == true ||
        e.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
            "Sin conexión a internet. Verifica tu red."
        else -> "Código inválido. Verifica e intenta nuevamente."
    }

    private fun parseUpdatePasswordError(e: Throwable): String = when {
        e.message?.contains("Password should be at least", ignoreCase = true) == true ->
            "La contraseña debe tener al menos 6 caracteres."
        e.message?.contains("same_password", ignoreCase = true) == true ->
            "La nueva contraseña debe ser diferente a la anterior."
        e.message?.contains("network", ignoreCase = true) == true ||
        e.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
            "Sin conexión a internet. Verifica tu red."
        else -> "Error al cambiar la contraseña. Intenta nuevamente."
    }
}

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data object Success : AuthUiState()
    data object EmailPendingConfirmation : AuthUiState()
    data object ResetEmailSent : AuthUiState()
    data object ForgotPasswordIdle : AuthUiState()
    data object OtpVerified : AuthUiState()
    data object PasswordResetSuccess : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
