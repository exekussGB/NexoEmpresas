package cl.nexo.empresas.data.repository

import cl.nexo.empresas.data.model.GraficoData
import cl.nexo.empresas.domain.repository.GraficosRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class GraficosRepositoryImpl @Inject constructor(
    private val client: SupabaseClient
) : GraficosRepository {

    override suspend fun getGraficoData(empresaId: String, meses: Int): Result<GraficoData> =
        runCatching {
            client.auth.awaitInitialization()
            // El RPC retorna un jsonb directo → decodeAs (no decodeSingle que espera array)
            client.postgrest
                .rpc("get_grafico_data", buildJsonObject {
                    put("p_empresa_id", empresaId)
                    put("p_meses", meses)
                })
                .decodeAs<GraficoData>()
        }
}
