package cl.nexo.empresas.domain.repository

import cl.nexo.empresas.data.model.DashboardTotales

interface DashboardRepository {
    suspend fun getTotales(): Result<DashboardTotales>
}
