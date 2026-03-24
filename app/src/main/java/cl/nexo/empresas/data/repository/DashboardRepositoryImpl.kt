package cl.nexo.empresas.data.repository

import cl.nexo.empresas.core.session.SessionManager
import cl.nexo.empresas.data.model.DashboardTotales
import cl.nexo.empresas.domain.repository.DashboardRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val sessionManager: SessionManager
) : DashboardRepository {

    override suspend fun getTotales(): Result<DashboardTotales> = runCatching {
        // El RPC retorna un objeto JSON plano {}, NO un array []
        // Por eso usamos decodeAs en vez de decodeSingle
        supabase.postgrest.rpc(
            "get_dashboard_totals",
            buildJsonObject { put("p_empresa_id", sessionManager.empresaId) }
        ).decodeAs<DashboardTotales>()
    }
}
