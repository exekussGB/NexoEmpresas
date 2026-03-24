package cl.nexo.empresas.domain.repository

import cl.nexo.empresas.data.model.CuentaCorriente

interface CuentasCorrientesRepository {
    suspend fun getCuentas(empresaId: String): Result<List<CuentaCorriente>>
    suspend fun createCuenta(cuenta: CuentaCorriente): Result<CuentaCorriente>
    suspend fun updateCuenta(cuenta: CuentaCorriente): Result<CuentaCorriente>
    suspend fun toggleActiva(cuentaId: String, activa: Boolean): Result<Unit>
}
