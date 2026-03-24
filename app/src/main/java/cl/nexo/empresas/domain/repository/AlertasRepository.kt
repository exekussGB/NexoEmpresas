package cl.nexo.empresas.domain.repository

import cl.nexo.empresas.data.model.AlertaConfig

interface AlertasRepository {
    suspend fun getConfig(): Result<AlertaConfig?>
    suspend fun saveConfig(config: AlertaConfig): Result<Unit>
}
