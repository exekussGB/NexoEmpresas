package com.nexo.empresas.data.repository

import com.nexo.empresas.core.session.TenantManager
import com.nexo.empresas.data.model.Empresa
import com.nexo.empresas.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import javax.inject.Inject

// ── Modelo intermedio para empresa_usuarios ───────────────────────────────────

@Serializable
private data class EmpresaUsuario(
    val empresa_id: String
)

class AuthRepositoryImpl @Inject constructor(
    private val client: SupabaseClient,
    private val tenantManager: TenantManager
) : AuthRepository {

    override val sessionStatus: Flow<SessionStatus> = client.auth.sessionStatus

    override suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        // Cargar empresa inmediatamente después del login exitoso
        loadEmpresaForCurrentUser()
    }

    override suspend fun register(email: String, password: String): Result<Unit> = runCatching {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun logout() {
        tenantManager.clearEmpresa()
        client.auth.signOut()
    }

    override fun isLoggedIn(): Boolean =
        client.auth.currentSessionOrNull() != null

    override fun currentUserId(): String? =
        client.auth.currentUserOrNull()?.id

    /**
     * Consulta empresa_usuarios para obtener la empresa del usuario autenticado,
     * luego carga los datos completos de empresas y los registra en TenantManager.
     *
     * Se llama en dos momentos:
     *   1. Después del login manual (desde login())
     *   2. Al restaurar sesión (desde AuthViewModel al detectar Authenticated)
     */
    override suspend fun loadEmpresaForCurrentUser(): Result<Unit> = runCatching {
        val userId = client.auth.currentUserOrNull()?.id
            ?: error("No hay usuario autenticado")

        // 1. Obtener empresa_id desde empresa_usuarios
        val empresaUsuario = client.postgrest["empresa_usuarios"]
            .select {
                filter { eq("user_id", userId) }
                limit(1)
            }
            .decodeSingle<EmpresaUsuario>()

        // 2. Cargar datos completos de la empresa
        val empresa = client.postgrest["empresas"]
            .select {
                filter { eq("id", empresaUsuario.empresa_id) }
                limit(1)
            }
            .decodeSingle<Empresa>()

        // 3. Registrar en TenantManager
        tenantManager.empresa = empresa
    }
}
