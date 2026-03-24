package cl.nexo.empresas.data.repository

import cl.nexo.empresas.core.session.SessionManager
import cl.nexo.empresas.data.model.DashboardTotales
import cl.nexo.empresas.domain.repository.DashboardRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class DashboardParams(
    @SerialName("p_empresa_id") val pEmpresaId: String
)

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val sessionManager: SessionManager
) : DashboardRepository {

    override suspend fun getTotales(): Result<DashboardTotales> = runCatching {
        supabase.postgrest.rpc(
            "get_dashboard_totals",
            DashboardParams(sessionManager.empresaId)
        ).decodeAs<DashboardTotales>()
    }
}
