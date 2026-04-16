package com.nexo.empresas.presentation.cuentas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexo.empresas.core.session.TenantManager
import com.nexo.empresas.data.model.CuentaCorriente
import com.nexo.empresas.domain.repository.CuentasCorrientesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CuentasCorrientesViewModel @Inject constructor(
    private val repo: CuentasCorrientesRepository,
    private val tenantManager: TenantManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<CuentasUiState>(CuentasUiState.Loading)
    val uiState: StateFlow<CuentasUiState> = _uiState

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog

    private val _editTarget = MutableStateFlow<CuentaCorriente?>(null)
    val editTarget: StateFlow<CuentaCorriente?> = _editTarget

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    // Filtro: null = todas, true = activas, false = inactivas
    private val _filtroActiva = MutableStateFlow<Boolean?>(null)
    val filtroActiva: StateFlow<Boolean?> = _filtroActiva

    private var allCuentas: List<CuentaCorriente> = emptyList()

    fun loadCuentas() {
        val empresaId = tenantManager.empresaId ?: return
        viewModelScope.launch {
            _uiState.value = CuentasUiState.Loading
            repo.getCuentas(empresaId)
                .onSuccess { list ->
                    allCuentas = list
                    applyFilter()
                }
                .onFailure {
                    _uiState.value = CuentasUiState.Error(it.message ?: "Error al cargar cuentas")
                }
        }
    }

    fun setFiltro(activa: Boolean?) {
        _filtroActiva.value = activa
        applyFilter()
    }

    private fun applyFilter() {
        val filtro = _filtroActiva.value
        val filtered = if (filtro == null) allCuentas else allCuentas.filter { it.activa == filtro }
        _uiState.value = CuentasUiState.Success(filtered)
    }

    fun openCreateDialog() {
        _editTarget.value = null
        _saveState.value = SaveState.Idle
        _showDialog.value = true
    }

    fun openEditDialog(cuenta: CuentaCorriente) {
        _editTarget.value = cuenta
        _saveState.value = SaveState.Idle
        _showDialog.value = true
    }

    fun closeDialog() {
        _showDialog.value = false
        _saveState.value = SaveState.Idle
    }

    fun save(
        nombre: String,
        tipo: String,
        numeroCuenta: String?,
        saldoInicial: Long,
        activa: Boolean
    ) {
        val empresaId = tenantManager.empresaId ?: return
        viewModelScope.launch {
            _saveState.value = SaveState.Loading
            val target = _editTarget.value

            val result = if (target == null) {
                repo.createCuenta(
                    CuentaCorriente(
                        empresaId    = empresaId,
                        nombre       = nombre.trim(),
                        tipo         = tipo,
                        numeroCuenta = numeroCuenta?.trim()?.ifEmpty { null },
                        saldoInicial = saldoInicial,
                        activa       = activa
                    )
                )
            } else {
                repo.updateCuenta(
                    target.copy(
                        nombre       = nombre.trim(),
                        tipo         = tipo,
                        numeroCuenta = numeroCuenta?.trim()?.ifEmpty { null },
                        saldoInicial = saldoInicial,
                        activa       = activa
                    )
                )
            }

            result
                .onSuccess {
                    _saveState.value = SaveState.Success
                    _showDialog.value = false
                    loadCuentas()
                }
                .onFailure {
                    _saveState.value = SaveState.Error(it.message ?: "Error al guardar")
                }
        }
    }

    fun toggleActiva(cuenta: CuentaCorriente) {
        viewModelScope.launch {
            repo.toggleActiva(cuenta.id, !cuenta.activa)
                .onSuccess { loadCuentas() }
        }
    }
}

sealed class CuentasUiState {
    data object Loading : CuentasUiState()
    data class Success(val cuentas: List<CuentaCorriente>) : CuentasUiState()
    data class Error(val message: String) : CuentasUiState()
}

sealed class SaveState {
    data object Idle    : SaveState()
    data object Loading : SaveState()
    data object Success : SaveState()
    data class Error(val message: String) : SaveState()
}
