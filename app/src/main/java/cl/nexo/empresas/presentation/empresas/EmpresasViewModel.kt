package cl.nexo.empresas.presentation.empresas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.nexo.empresas.core.session.SessionManager
import cl.nexo.empresas.core.session.TenantManager
import cl.nexo.empresas.data.model.Empresa
import cl.nexo.empresas.data.model.EmpresaMember
import cl.nexo.empresas.domain.repository.AlertasRepository
import cl.nexo.empresas.domain.repository.EmpresasRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmpresasViewModel @Inject constructor(
    private val empresasRepository: EmpresasRepository,
    private val tenantManager: TenantManager,
    private val sessionManager: SessionManager,
    private val client: SupabaseClient,
    private val alertasRepository: AlertasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EmpresasUiState>(EmpresasUiState.Idle)
    val uiState: StateFlow<EmpresasUiState> = _uiState

    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> = _showCreateDialog

    private val _createState = MutableStateFlow<CreateEmpresaState>(CreateEmpresaState.Idle)
    val createState: StateFlow<CreateEmpresaState> = _createState

    val selectedEmpresa: Empresa? get() = tenantManager.empresa

    fun loadEmpresas() {
        viewModelScope.launch {
            _uiState.value = EmpresasUiState.Loading
            empresasRepository.getEmpresasForUser()
                .onSuccess { _uiState.value = EmpresasUiState.Success(it) }
                .onFailure { _uiState.value = EmpresasUiState.Error(it.message ?: "Error al cargar empresas") }
        }
    }

    /**
     * Selecciona la empresa activa y, de forma async, carga el rol del usuario
     * en esa empresa para que isOwner() funcione en toda la app.
     * También registra el token FCM para recibir push notifications.
     */
    fun selectEmpresa(empresa: Empresa) {
        tenantManager.empresa = empresa
        viewModelScope.launch {
            try {
                val userId = client.auth.currentUserOrNull()?.id ?: return@launch
                val members = client.postgrest["empresa_members"]
                    .select {
                        filter {
                            eq("empresa_id", empresa.id)
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<EmpresaMember>()
                sessionManager.userRole = members.firstOrNull()?.rol ?: "viewer"
            } catch (e: Exception) {
                // Fallback: si el usuario creó esta empresa, es el owner
                val userId = runCatching { client.auth.currentUserOrNull()?.id }.getOrNull()
                if (userId != null && empresa.createdBy == userId) {
                    sessionManager.userRole = "owner"
                }
            }
            // Registrar token FCM para esta empresa
            registrarFcmToken()
        }
    }

    /**
     * Obtiene el token FCM actual y lo guarda en alertas_config
     * para que las Edge Functions puedan enviar push notifications.
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
