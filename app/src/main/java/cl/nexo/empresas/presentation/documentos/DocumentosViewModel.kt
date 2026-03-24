package cl.nexo.empresas.presentation.documentos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.nexo.empresas.core.session.SessionManager
import cl.nexo.empresas.data.model.Documento
import cl.nexo.empresas.domain.repository.DocumentosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DocumentosUiState(
    val documentos: List<Documento> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val filtroEstado: String? = "pendiente",   // null = todos
    val filtroDias: Int? = null                 // null = todos, o 7/15/30/60
)

@HiltViewModel
class DocumentosViewModel @Inject constructor(
    private val repository: DocumentosRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentosUiState())
    val uiState: StateFlow<DocumentosUiState> = _uiState.asStateFlow()

    private var currentTipo: String = "ingreso"

    fun init(tipo: String) {
        currentTipo = tipo
        cargarDocumentos()
    }

    fun setFiltroEstado(estado: String?) {
        _uiState.value = _uiState.value.copy(filtroEstado = estado)
        cargarDocumentos()
    }

    fun setFiltroDias(dias: Int?) {
        _uiState.value = _uiState.value.copy(filtroDias = dias)
        cargarDocumentos()
    }

    fun cargarDocumentos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val empresaId = sessionManager.currentEmpresaId ?: ""
            val estado = _uiState.value.filtroEstado
            repository.getDocumentos(empresaId, currentTipo, estado)
                .onSuccess { docs ->
                    val filtered = filterByDias(docs, _uiState.value.filtroDias)
                    _uiState.value = _uiState.value.copy(documentos = filtered, isLoading = false)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
                }
        }
    }

    fun marcarPagado(id: String, fechaPago: String, numeroSeguimiento: String?) {
        viewModelScope.launch {
            repository.marcarPagado(id, fechaPago, numeroSeguimiento)
                .onSuccess { cargarDocumentos() }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
        }
    }

    fun anular(id: String) {
        viewModelScope.launch {
            repository.anularDocumento(id)
                .onSuccess { cargarDocumentos() }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
        }
    }

    fun isOwner(): Boolean = sessionManager.currentRol == "owner"

    private fun filterByDias(docs: List<Documento>, dias: Int?): List<Documento> {
        if (dias == null) return docs
        val hoy = java.time.LocalDate.now()
        val limite = hoy.plusDays(dias.toLong())
        return docs.filter {
            try {
                val fecha = java.time.LocalDate.parse(it.fechaVencimiento)
                !fecha.isAfter(limite)
            } catch (e: Exception) { true }
        }
    }
}
