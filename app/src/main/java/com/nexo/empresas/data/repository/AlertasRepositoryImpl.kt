package com.nexo.empresas.data.repository

import com.nexo.empresas.core.session.SessionManager
import com.nexo.empresas.data.model.AlertaConfig
import com.nexo.empresas.domain.repository.AlertasRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertasRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val sessionManager: SessionManager
) : AlertasRepository {

    override suspend fun getConfig(): Result<AlertaConfig?> = runCatching {
        supabase.from("alertas_config").select {
            filter {
                eq("empresa_id", sessionManager.empresaId)
                eq("user_id", sessionManager.userId)
            }
        }.decodeSingleOrNull<AlertaConfig>()
    }

    override suspend fun saveConfig(config: AlertaConfig): Result<Unit> = runCatching {
        val withUser = config.copy(
            empresaId = sessionManager.empresaId,
            userId = sessionManager.userId
        )
        supabase.from("alertas_config").upsert(withUser) {
            onConflict = "empresa_id,user_id"
        }
    }

    /**
     * Guarda el FCM token del dispositivo en alertas_config.
     * Si no existe registro aún, crea uno con valores por defecto.
     */
    override suspend fun saveFcmToken(token: String): Result<Unit> = runCatching {
        val empresaId = sessionManager.empresaId
        val userId    = sessionManager.userId
        if (empresaId.isBlank() || userId.isBlank()) return@runCatching

        // Intentamos actualizar primero
        val rows = supabase.from("alertas_config").select {
            filter {
                eq("empresa_id", empresaId)
                eq("user_id", userId)
            }
        }.decodeSingleOrNull<AlertaConfig>()

        if (rows != null) {
            supabase.from("alertas_config").update(
                { set("fcm_token", token) }
            ) {
                filter {
                    eq("empresa_id", empresaId)
                    eq("user_id", userId)
                }
            }
        } else {
            // Upsert con config por defecto + token
            val defaultConfig = AlertaConfig(
                empresaId  = empresaId,
                userId     = userId,
                fcmToken   = token
            )
            supabase.from("alertas_config").upsert(defaultConfig) {
                onConflict = "empresa_id,user_id"
            }
        }
    }
}
