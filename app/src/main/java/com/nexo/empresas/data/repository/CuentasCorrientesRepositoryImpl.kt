package com.nexo.empresas.data.repository

import com.nexo.empresas.core.util.Constants
import com.nexo.empresas.data.model.CuentaCorriente
import com.nexo.empresas.data.model.CuentaCorrienteUpdate
import com.nexo.empresas.domain.repository.CuentasCorrientesRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject

class CuentasCorrientesRepositoryImpl @Inject constructor(
    private val client: SupabaseClient
) : CuentasCorrientesRepository {

    private suspend fun ensureSession() {
        client.auth.awaitInitialization()
    }

    override suspend fun getCuentas(empresaId: String): Result<List<CuentaCorriente>> = runCatching {
        ensureSession()
        client.from(Constants.TABLE_CUENTAS_CORRIENTES)
            .select {
                filter { eq("empresa_id", empresaId) }
            }
            .decodeList<CuentaCorriente>()
            .sortedBy { it.nombre }
    }

    override suspend fun createCuenta(cuenta: CuentaCorriente): Result<CuentaCorriente> = runCatching {
        ensureSession()
        client.from(Constants.TABLE_CUENTAS_CORRIENTES)
            .insert(cuenta) { select() }
            .decodeSingle<CuentaCorriente>()
    }

    override suspend fun updateCuenta(cuenta: CuentaCorriente): Result<CuentaCorriente> = runCatching {
        ensureSession()
        // CuentaCorrienteUpdate evita el bug de encodeDefaults=false
        val payload = CuentaCorrienteUpdate(
            nombre       = cuenta.nombre,
            tipo         = cuenta.tipo,
            numeroCuenta = cuenta.numeroCuenta,
            saldoInicial = cuenta.saldoInicial,
            activa       = cuenta.activa
        )
        client.from(Constants.TABLE_CUENTAS_CORRIENTES)
            .update(payload) {
                select()
                filter { eq("id", cuenta.id) }
            }
            .decodeSingle<CuentaCorriente>()
    }

    override suspend fun toggleActiva(cuentaId: String, activa: Boolean): Result<Unit> = runCatching {
        ensureSession()
        client.from(Constants.TABLE_CUENTAS_CORRIENTES)
            .update(mapOf("activa" to activa)) {
                filter { eq("id", cuentaId) }
            }
    }
}
