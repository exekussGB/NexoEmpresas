package cl.nexo.empresas.data.repository

import cl.nexo.empresas.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.OtpType
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val client: SupabaseClient
) : AuthRepository {

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

    /**
     * Envía un email de recuperación de contraseña con un OTP de 6 dígitos.
     * Requiere que en el dashboard de Supabase esté habilitado "Email OTP".
     */
    override suspend fun resetPassword(email: String): Result<Unit> = runCatching {
        client.auth.resetPasswordForEmail(email)
    }

    /**
     * Verifica el OTP de recuperación. Si es correcto, Supabase establece
     * automáticamente la sesión autenticada, permitiendo llamar a updatePassword().
     */
    override suspend fun verifyRecoveryOtp(email: String, code: String): Result<Unit> = runCatching {
        client.auth.verifyEmailOtp(
            type = OtpType.Email.RECOVERY,
            email = email,
            token = code
        )
    }

    /**
     * Actualiza la contraseña del usuario autenticado (sesión activa tras verifyRecoveryOtp).
     */
    override suspend fun updatePassword(newPassword: String): Result<Unit> = runCatching {
        client.auth.updateUser {
            password = newPassword
        }
    }
}
