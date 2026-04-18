package com.nexo.empresas.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexo.empresas.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val sessionStatus: StateFlow<SessionStatus> = authRepository.sessionStatus
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionStatus.Initializing
        )

    val isLoggedIn: StateFlow<Boolean?> = sessionStatus
        .map { status ->
            when (status) {
                is SessionStatus.Authenticated -> true
                is SessionStatus.NotAuthenticated -> false
                is SessionStatus.Initializing -> null
                else -> null
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.sessionStatus.collect { status ->
                println("DEBUG: sessionStatus = $status")
                if (status is SessionStatus.Authenticated) {
                    authRepository.loadEmpresaForCurrentUser()
                        .onSuccess {
                            println("DEBUG: empresa cargada OK")
                        }
                        .onFailure { e ->
                            println("DEBUG: ERROR cargando empresa = ${e.message}")
                            println("DEBUG: ERROR causa = ${e.cause}")
                        }
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.login(email, password)
                .onSuccess { _uiState.value = AuthUiState.Success }
                .onFailure { e -> _uiState.value = AuthUiState.Error(parseLoginError(e)) }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.register(email, password)
                .onSuccess {
                    authRepository.login(email, password)
                        .onSuccess { _uiState.value = AuthUiState.Success }
                        .onFailure { _uiState.value = AuthUiState.EmailPendingConfirmation }
                }
                .onFailure { e -> _uiState.value = AuthUiState.Error(parseRegisterError(e)) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
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
}

sealed class AuthUiState {
    data object Idle                       : AuthUiState()
    data object Loading                    : AuthUiState()
    data object Success                    : AuthUiState()
    data object EmailPendingConfirmation   : AuthUiState()
    data class  Error(val message: String) : AuthUiState()
}
