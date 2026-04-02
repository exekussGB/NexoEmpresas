package cl.nexo.empresas.domain.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun verifyRecoveryOtp(email: String, code: String): Result<Unit>
    suspend fun updatePassword(newPassword: String): Result<Unit>
    suspend fun logout()
    fun isLoggedIn(): Boolean
    fun currentUserId(): String?
}
