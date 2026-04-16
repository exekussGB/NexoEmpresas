package com.nexo.empresas.presentation.cheques

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexo.empresas.core.session.SessionManager
import com.nexo.empresas.data.model.Cheque
import com.nexo.empresas.domain.repository.ChequesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChequesUiState(
    val pendientes: List<Cheque> = emptyList(),
    val cobrados: List<Cheque> = emptyList(),
    val rechazados: List<Cheque> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChequesViewModel @Inject constructor(
    private val repository: ChequesRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChequesUiState())
    val uiState: StateFlow<ChequesUiState> = _uiState.asStateFlow()

    init { cargarCheques() }

    fun cargarCheques() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val empresaId = sessionManager.currentEmpresaId ?: ""
            repository.getCheques(empresaId)
                .onSuccess { cheques ->
                    _uiState.value = _uiState.value.copy(
                        pendientes = cheques.filter { it.estado == "pendiente" },
                        cobrados   = cheques.filter { it.estado == "cobrado" },
                        rechazados = cheques.filter { it.estado == "rechazado" },
                        isLoading  = false
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
                }
        }
    }

    fun actualizarEstado(chequeId: String, estado: String) {
        viewModelScope.launch {
            repository.actualizarEstadoCheque(chequeId, estado)
                .onSuccess { cargarCheques() }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }

    fun isOwner(): Boolean = sessionManager.currentRol == "owner"
}
