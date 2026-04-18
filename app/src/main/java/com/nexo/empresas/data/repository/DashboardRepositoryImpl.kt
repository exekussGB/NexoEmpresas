package com.nexo.empresas.data.repository

import com.nexo.empresas.core.session.SessionManager
import com.nexo.empresas.data.model.DashboardTotales
import com.nexo.empresas.domain.repository.DashboardRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de Dashboard.
 * Consume funciones remotas (RPC) de Supabase para obtener agregados financieros en tiempo real.
 * Proporciona los datos para las tarjetas de resumen y gráficos de la pantalla principal.
 */
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
