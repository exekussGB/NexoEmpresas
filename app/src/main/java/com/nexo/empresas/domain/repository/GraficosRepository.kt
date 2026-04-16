package com.nexo.empresas.domain.repository

import com.nexo.empresas.data.model.GraficoData

interface GraficosRepository {
    suspend fun getGraficoData(empresaId: String, meses: Int): Result<GraficoData>
}
