package com.nexo.empresas.data.repository

import com.nexo.empresas.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val client: SupabaseClient
) : AuthRepository {

    override val sessionStatus: Flow<SessionStatus> = client.auth.sessionStatus

    override suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun register(email: String, password: String): Result<Unit> = runCatching {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun logout() {
        client.auth.signOut()
    }

    /**
     * Verifica si hay una sesión activa con access token válido.
     * currentSessionOrNull() confirma que hay token, no solo usuario cacheado.
     */
    override fun isLoggedIn(): Boolean =
        client.auth.currentSessionOrNull() != null

    override fun currentUserId(): String? =
        client.auth.currentUserOrNull()?.id
}
