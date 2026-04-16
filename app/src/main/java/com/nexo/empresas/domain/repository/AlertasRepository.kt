package com.nexo.empresas.domain.repository

import com.nexo.empresas.data.model.AlertaConfig

interface AlertasRepository {
    suspend fun getConfig(): Result<AlertaConfig?>
    suspend fun saveConfig(config: AlertaConfig): Result<Unit>
    /** Guarda o actualiza el FCM token para el usuario/empresa actual. */
    suspend fun saveFcmToken(token: String): Result<Unit>
}
