package com.nexo.empresas.domain.repository

import com.nexo.empresas.data.model.DashboardTotales

interface DashboardRepository {
    suspend fun getTotales(): Result<DashboardTotales>
}
