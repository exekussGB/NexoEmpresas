package cl.nexo.empresas.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.nexo.empresas.core.session.SessionManager
import cl.nexo.empresas.core.util.Constants
import cl.nexo.empresas.data.model.Empresa
import cl.nexo.empresas.data.model.EmpresaMember
import cl.nexo.empresas.data.model.InvitacionPendiente
import cl.nexo.empresas.domain.repository.EmpresasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class InviteState {
    data object Idle : InviteState()
    data object Loading : InviteState()
    data class Success(val message: String) : InviteState()
    data class Error(val message: String) : InviteState()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val empresasRepository: EmpresasRepository,
    private val sessionManager: SessionManager,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _empresa = MutableStateFlow<Empresa?>(null)
    val empresa: StateFlow<Empresa?> = _empresa.asStateFlow()

    val userEmail: String get() = sessionManager.userEmail
    val userRole: String get() = sessionManager.userRole

    val inviteCode: StateFlow<String> get() = _inviteCode
    private val _inviteCode = MutableStateFlow("")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ── Invitaciones ────────────────────────────────────────────────────────
    private val _invitaciones = MutableStateFlow<List<InvitacionPendiente>>(emptyList())
    val invitaciones: StateFlow<List<InvitacionPendiente>> = _invitaciones.asStateFlow()

    private val _inviteState = MutableStateFlow<InviteState>(InviteState.Idle)
    val inviteState: StateFlow<InviteState> = _inviteState.asStateFlow()

    init {
        loadEmpresa()
        loadInvitaciones()
    }

    private fun loadEmpresa() {
        viewModelScope.launch {
            _isLoading.value = true
            empresasRepository.getEmpresasForUser()
                .onSuccess { empresas ->
                    val emp = empresas.find { it.id == sessionManager.empresaId }
                    _empresa.value = emp
                    _inviteCode.value = emp?.inviteCode ?: ""
                }
                .onFailure { /* silently ignore */ }
            _isLoading.value = false
        }
    }

    fun loadInvitaciones() {
        val empresaId = sessionManager.empresaId
        if (empresaId.isBlank()) return

        viewModelScope.launch {
            runCatching {
                supabase.auth.awaitInitialization()
                supabase.from(Constants.TABLE_INVITACIONES)
                    .select {
                        filter {
                            eq("empresa_id", empresaId)
                            eq("estado", "pendiente")
                        }
                    }
                    .decodeList<InvitacionPendiente>()
            }.onSuccess { list ->
                _invitaciones.value = list
            }.onFailure {
                _invitaciones.value = emptyList()
            }
        }
    }

    fun invitarUsuario(email: String) {
        val trimmedEmail = email.trim().lowercase()

        // Validación de email
        if (trimmedEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _inviteState.value = InviteState.Error("Ingresa un correo electrónico válido")
            return
        }

        val empresaId = sessionManager.empresaId
        val userId = sessionManager.userId
        if (empresaId.isBlank() || userId.isBlank()) {
            _inviteState.value = InviteState.Error("Error de sesión. Intenta nuevamente.")
            return
        }

        _inviteState.value = InviteState.Loading

        viewModelScope.launch {
            runCatching {
                supabase.auth.awaitInitialization()

                // Verificar si ya existe una invitación pendiente para ese email
                val existentes = supabase.from(Constants.TABLE_INVITACIONES)
                    .select {
                        filter {
                            eq("empresa_id", empresaId)
                            eq("email_invitado", trimmedEmail)
                            eq("estado", "pendiente")
                        }
                    }
                    .decodeList<InvitacionPendiente>()

                if (existentes.isNotEmpty()) {
                    throw IllegalStateException("Ya existe una invitación pendiente para este correo")
                }

                val invitacion = InvitacionPendiente(
                    empresaId = empresaId,
                    emailInvitado = trimmedEmail,
                    estado = "pendiente",
                    invitadoPor = userId
                )
                supabase.from(Constants.TABLE_INVITACIONES)
                    .insert(invitacion)
            }.onSuccess {
                _inviteState.value = InviteState.Success("Invitación enviada a $trimmedEmail")
                loadInvitaciones()
            }.onFailure { e ->
                val msg = when {
                    e is IllegalStateException -> e.message ?: "Error al enviar invitación"
                    else -> "Error al enviar invitación: ${e.message}"
                }
                _inviteState.value = InviteState.Error(msg)
            }
        }
    }

    fun aceptarInvitacion(invitacion: InvitacionPendiente) {
        viewModelScope.launch {
            _inviteState.value = InviteState.Loading
            runCatching {
                supabase.auth.awaitInitialization()

                // Llamar a la función RPC que resuelve el user_id y crea el membership
                val result = supabase.postgrest.rpc(
                    "aceptar_invitacion",
                    buildJsonObject {
                        put("p_invitacion_id", invitacion.id)
                    }
                ).decodeAs<Boolean>()

                if (!result) {
                    // El usuario aún no se registra, pero la invitación quedó aceptada
                    // Se asignará automáticamente cuando se registre
                }
                result
            }.onSuccess { registered ->
                val msg = if (registered)
                    "Acceso confirmado para ${invitacion.emailInvitado}"
                else
                    "Invitación aceptada. ${invitacion.emailInvitado} recibirá acceso cuando se registre."
                _inviteState.value = InviteState.Success(msg)
                loadInvitaciones()
            }.onFailure { e ->
                _inviteState.value = InviteState.Error("Error al aceptar invitación: ${e.message}")
            }
        }
    }

    fun rechazarInvitacion(invitacion: InvitacionPendiente) {
        viewModelScope.launch {
            _inviteState.value = InviteState.Loading
            runCatching {
                supabase.auth.awaitInitialization()
                supabase.from(Constants.TABLE_INVITACIONES)
                    .delete {
                        filter { eq("id", invitacion.id) }
                    }
            }.onSuccess {
                _inviteState.value = InviteState.Success("Invitación de ${invitacion.emailInvitado} rechazada")
                loadInvitaciones()
            }.onFailure { e ->
                _inviteState.value = InviteState.Error("Error al rechazar invitación: ${e.message}")
            }
        }
    }

    fun resetInviteState() {
        _inviteState.value = InviteState.Idle
    }

    fun logout() {
        sessionManager.clearSession()
    }
}
