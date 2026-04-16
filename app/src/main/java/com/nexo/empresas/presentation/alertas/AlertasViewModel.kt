package com.nexo.empresas.presentation.alertas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexo.empresas.core.session.SessionManager
import com.nexo.empresas.data.model.AlertaConfig
import com.nexo.empresas.domain.repository.AlertasRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertasViewModel @Inject constructor(
    private val alertasRepository: AlertasRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _config = MutableStateFlow<AlertaConfig?>(null)
    val config: StateFlow<AlertaConfig?> = _config.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadConfig()
        registrarFcmToken()
    }

    private fun loadConfig() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            alertasRepository.getConfig()
                .onSuccess { config ->
                    _config.value = config ?: AlertaConfig(
                        empresaId = sessionManager.empresaId,
                        userId = sessionManager.userId
                    )
                }
                .onFailure { _error.value = it.message ?: "Error al cargar configuración" }
            _isLoading.value = false
        }
    }

    /**
     * Obtiene el token FCM actual del dispositivo y lo guarda en Supabase.
     * Se ejecuta cada vez que se abre la pantalla de alertas para mantenerlo actualizado.
     */
    private fun registrarFcmToken() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                viewModelScope.launch {
                    alertasRepository.saveFcmToken(token)
                        .onFailure { /* ignorar silenciosamente */ }
                }
            }
            .addOnFailureListener { /* ignorar si Firebase no está disponible */ }
    }

    fun saveConfig(
        dias: Int,
        cobros: Boolean,
        pagos: Boolean,
        cheques: Boolean
    ) {
        val current = _config.value ?: AlertaConfig(
            empresaId = sessionManager.empresaId,
            userId = sessionManager.userId
        )
        val updated = current.copy(
            diasAnticipacion = dias,
            alertasCobros = cobros,
            alertasPagos = pagos,
            alertasCheques = cheques
        )
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isSaved.value = false
            alertasRepository.saveConfig(updated)
                .onSuccess {
                    _config.value = updated
                    _isSaved.value = true
                }
                .onFailure { _error.value = it.message ?: "Error al guardar configuración" }
            _isLoading.value = false
        }
    }

    fun clearSaved() {
        _isSaved.value = false
    }
}
