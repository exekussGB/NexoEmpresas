package cl.nexo.empresas.data.repository

import cl.nexo.empresas.core.session.SessionManager
import cl.nexo.empresas.data.model.AlertaConfig
import cl.nexo.empresas.domain.repository.AlertasRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
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
}
