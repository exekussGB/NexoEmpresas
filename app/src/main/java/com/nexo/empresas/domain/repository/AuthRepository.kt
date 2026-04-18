package com.nexo.empresas.domain.repository

import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val sessionStatus: Flow<SessionStatus>
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String): Result<Unit>
    suspend fun logout()
    fun isLoggedIn(): Boolean
    fun currentUserId(): String?
}
