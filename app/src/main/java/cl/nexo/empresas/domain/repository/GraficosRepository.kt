package cl.nexo.empresas.domain.repository

import cl.nexo.empresas.data.model.GraficoData

interface GraficosRepository {
    suspend fun getGraficoData(empresaId: String, meses: Int): Result<GraficoData>
}
