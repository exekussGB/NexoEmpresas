package cl.nexo.empresas.presentation.empresas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.nexo.empresas.data.model.Empresa
import cl.nexo.empresas.domain.repository.EmpresasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmpresasViewModel @Inject constructor(
    private val empresasRepository: EmpresasRepository,
    private val client: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow<EmpresasUiState>(EmpresasUiState.Idle)
    val uiState: StateFlow<EmpresasUiState> = _uiState

    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> = _showCreateDialog

    private val _createState = MutableStateFlow<CreateEmpresaState>(CreateEmpresaState.Idle)
    val createState: StateFlow<CreateEmpresaState> = _createState

    var selectedEmpresa: Empresa? = null
        private set

    fun loadEmpresas() {
        viewModelScope.launch {
            _uiState.value = EmpresasUiState.Loading
            empresasRepository.getEmpresasForUser()
                .onSuccess { _uiState.value = EmpresasUiState.Success(it) }
                .onFailure { _uiState.value = EmpresasUiState.Error(it.message ?: "Error al cargar empresas") }
        }
    }

    fun selectEmpresa(empresa: Empresa) {
        selectedEmpresa = empresa
    }

    fun showCreateDialog(show: Boolean) {
        _showCreateDialog.value = show
        if (!show) _createState.value = CreateEmpresaState.Idle
    }

    fun createEmpresa(nombre: String, rut: String, giro: String) {
        viewModelScope.launch {
            _createState.value = CreateEmpresaState.Loading
            val empresa = Empresa(
                nombre = nombre.trim(),
                rut = rut.trim(),
                giro = giro.trim().ifEmpty { null }
            )
            empresasRepository.createEmpresa(empresa)
                .onSuccess {
                    _createState.value = CreateEmpresaState.Success
                    _showCreateDialog.value = false
                    loadEmpresas()
                }
                .onFailure {
                    _createState.value = CreateEmpresaState.Error(it.message ?: "Error al crear empresa")
                }
        }
    }
}

sealed class EmpresasUiState {
    data object Idle    : EmpresasUiState()
    data object Loading : EmpresasUiState()
    data class Success(val empresas: List<Empresa>) : EmpresasUiState()
    data class Error(val message: String) : EmpresasUiState()
}

sealed class CreateEmpresaState {
    data object Idle    : CreateEmpresaState()
    data object Loading : CreateEmpresaState()
    data object Success : CreateEmpresaState()
    data class Error(val message: String) : CreateEmpresaState()
}