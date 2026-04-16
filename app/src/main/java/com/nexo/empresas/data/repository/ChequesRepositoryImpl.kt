package com.nexo.empresas.data.repository

import com.nexo.empresas.core.session.SessionManager
import com.nexo.empresas.data.model.Cheque
import com.nexo.empresas.domain.repository.ChequesRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChequesRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val sessionManager: SessionManager
) : ChequesRepository {

    override suspend fun getCheques(empresaId: String, estado: String?): Result<List<Cheque>> =
        runCatching {
            supabaseClient.postgrest["cheques"].select {
                filter {
                    eq("empresa_id", empresaId)
                    if (estado != null) eq("estado", estado)
                }
                order("fecha_cobro", Order.ASCENDING)
            }.decodeList()
        }

    override suspend fun getChequesDeDocumento(documentoId: String): Result<List<Cheque>> =
        runCatching {
            supabaseClient.postgrest["cheques"].select {
                filter { eq("documento_id", documentoId) }
                order("orden", Order.ASCENDING)
            }.decodeList()
        }

    override suspend fun actualizarEstadoCheque(chequeId: String, estado: String): Result<Unit> =
        runCatching {
            supabaseClient.postgrest["cheques"].update(mapOf("estado" to estado)) {
                filter { eq("id", chequeId) }
            }
        }

    override suspend fun getChequesVencimientoProximo(fechaLimite: String): Result<List<Cheque>> =
        runCatching {
            supabaseClient.postgrest["cheques"].select {
                filter {
                    eq("empresa_id", sessionManager.empresaId)
                    eq("estado", "pendiente")
                    lte("fecha_cobro", fechaLimite)
                }
                order("fecha_cobro", Order.ASCENDING)
            }.decodeList()
        }
}
