package cl.nexo.empresas.presentation.contactos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.nexo.empresas.core.session.TenantManager
import cl.nexo.empresas.data.model.Contacto
import cl.nexo.empresas.domain.repository.ContactosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FiltroContacto(val label: String) {
    TODOS("Todos"),
    PROVEEDOR("Proveedores"),
    CLIENTE("Clientes")
}

@HiltViewModel
class ContactosViewModel @Inject constructor(
    private val contactosRepository: ContactosRepository,
    private val tenantManager: TenantManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ContactosUiState>(ContactosUiState.Idle)
    val uiState: StateFlow<ContactosUiState> = _uiState

    private val _filtro = MutableStateFlow(FiltroContacto.TODOS)
    val filtro: StateFlow<FiltroContacto> = _filtro

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog

    private val _editando = MutableStateFlow<Contacto?>(null)
    val editando: StateFlow<Contacto?> = _editando

    private val _saveState = MutableStateFlow<SaveContactoState>(SaveContactoState.Idle)
    val saveState: StateFlow<SaveContactoState> = _saveState

    private var todosLosContactos: List<Contacto> = emptyList()

    fun loadContactos() {
        val empresaId = tenantManager.empresaId
        if (empresaId.isEmpty()) {
            _uiState.value = ContactosUiState.Error("No hay empresa seleccionada")
            return
        }
        viewModelScope.launch {
            _uiState.value = ContactosUiState.Loading
            contactosRepository.getContactos(empresaId)
                .onSuccess { lista ->
                    todosLosContactos = lista
                    aplicarFiltro()
                }
                .onFailure {
                    _uiState.value = ContactosUiState.Error(it.message ?: "Error al cargar contactos")
                }
        }
    }

    fun setFiltro(filtro: FiltroContacto) {
        _filtro.value = filtro
        aplicarFiltro()
    }

    private fun aplicarFiltro() {
        val filtrados = when (_filtro.value) {
            FiltroContacto.TODOS -> todosLosContactos
            FiltroContacto.PROVEEDOR -> todosLosContactos.filter { it.tipo == "proveedor" || it.tipo == "ambos" }
            FiltroContacto.CLIENTE -> todosLosContactos.filter { it.tipo == "cliente" || it.tipo == "ambos" }
        }
        _uiState.value = ContactosUiState.Success(filtrados)
    }

    fun showDialog(contacto: Contacto? = null) {
        _editando.value = contacto
        _saveState.value = SaveContactoState.Idle
        _showDialog.value = true
    }

    fun hideDialog() {
        _showDialog.value = false
        _editando.value = null
        _saveState.value = SaveContactoState.Idle
    }

    fun saveContacto(nombre: String, rut: String, tipo: String) {
        val empresaId = tenantManager.empresaId
        viewModelScope.launch {
            _saveState.value = SaveContactoState.Loading
            val contactoActual = _editando.value
            val result = if (contactoActual == null) {
                // Crear nuevo
                val nuevo = Contacto(
                    empresaId = empresaId,
                    nombre = nombre.trim(),
                    rut = rut.trim().ifEmpty { null },
                    tipo = tipo
                )
                contactosRepository.createContacto(nuevo)
            } else {
                // Actualizar existente
                val actualizado = contactoActual.copy(
                    nombre = nombre.trim(),
                    rut = rut.trim().ifEmpty { null },
                    tipo = tipo
                )
                contactosRepository.updateContacto(actualizado)
            }
            result
                .onSuccess {
                    _saveState.value = SaveContactoState.Success
                    _showDialog.value = false
                    loadContactos()
                }
                .onFailure {
                    _saveState.value = SaveContactoState.Error(it.message ?: "Error al guardar contacto")
                }
        }
    }

    fun toggleActivo(contacto: Contacto) {
        viewModelScope.launch {
            contactosRepository.toggleActivo(contacto.id, !contacto.activo)
                .onSuccess { loadContactos() }
                .onFailure {
                    _uiState.value = ContactosUiState.Error(it.message ?: "Error al actualizar contacto")
                }
        }
    }
}

sealed class ContactosUiState {
    data object Idle : ContactosUiState()
    data object Loading : ContactosUiState()
    data class Success(val contactos: List<Contacto>) : ContactosUiState()
    data class Error(val message: String) : ContactosUiState()
}

sealed class SaveContactoState {
    data object Idle : SaveContactoState()
    data object Loading : SaveContactoState()
    data object Success : SaveContactoState()
    data class Error(val message: String) : SaveContactoState()
}
