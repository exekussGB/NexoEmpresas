package cl.nexo.empresas.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.nexo.empresas.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

@Serializable
data class TeamMember(
    @SerialName("member_id") val memberId: String = "",
    @SerialName("user_id") val userId: String = "",
    val email: String = "",
    val rol: String = "viewer",
    @SerialName("created_at") val createdAt: String? = null
)

@HiltViewModel
class TeamMembersViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _members = MutableStateFlow<List<TeamMember>>(emptyList())
    val members: StateFlow<List<TeamMember>> = _members.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    val isOwner: Boolean get() = sessionManager.userRole == "owner"

    init {
        loadMembers()
    }

    fun loadMembers() {
        val empresaId = sessionManager.empresaId
        if (empresaId.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                supabase.auth.awaitInitialization()
                supabase.postgrest.rpc(
                    "get_team_members",
                    buildJsonObject { put("p_empresa_id", empresaId) }
                ).decodeList<TeamMember>()
            }.onSuccess {
                _members.value = it
            }.onFailure { e ->
                _uiMessage.value = "Error al cargar miembros: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    fun updateRole(memberId: String, newRole: String) {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                supabase.auth.awaitInitialization()
                supabase.postgrest.rpc(
                    "update_member_role",
                    buildJsonObject {
                        put("p_member_id", memberId)
                        put("p_new_role", newRole)
                    }
                )
            }.onSuccess {
                _uiMessage.value = "Rol actualizado correctamente"
                loadMembers()
            }.onFailure { e ->
                _uiMessage.value = "Error al cambiar rol: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    fun removeMember(memberId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                supabase.auth.awaitInitialization()
                supabase.postgrest.rpc(
                    "remove_team_member",
                    buildJsonObject { put("p_member_id", memberId) }
                )
            }.onSuccess {
                _uiMessage.value = "Miembro eliminado"
                loadMembers()
            }.onFailure { e ->
                _uiMessage.value = "Error al eliminar miembro: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    fun clearMessage() { _uiMessage.value = null }
}
