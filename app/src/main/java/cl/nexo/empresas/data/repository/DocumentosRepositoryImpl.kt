package cl.nexo.empresas.data.repository

import cl.nexo.empresas.data.model.*
import cl.nexo.empresas.domain.repository.DocumentosRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject

class DocumentosRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : DocumentosRepository {

    override suspend fun getDocumentos(empresaId: String, tipo: String, estado: String?): Result<List<Documento>> =
        runCatching {
            supabaseClient.postgrest["documentos"].select {
                filter {
                    eq("empresa_id", empresaId)
                    eq("tipo", tipo)
                    if (estado != null) eq("estado", estado)
                }
                order("fecha_vencimiento", Order.ASCENDING)
            }.decodeList<Documento>()
        }

    override suspend fun getDocumento(id: String): Result<Documento> =
        runCatching {
            supabaseClient.postgrest["documentos"].select {
                filter { eq("id", id) }
                limit(1)
                single()
            }.decodeSingle<Documento>()
        }

    override suspend fun addDocumento(doc: DocumentoCreate, cheques: List<ChequeCreate>): Result<String> =
        runCatching {
            val inserted = supabaseClient.postgrest["documentos"].insert(doc) {
                select()
            }.decodeSingle<Documento>()

            if (cheques.isNotEmpty()) {
                val chequesConId = cheques.map { it.copy(documentoId = inserted.id) }
                supabaseClient.postgrest["cheques"].insert(chequesConId)
            }
            inserted.id
        }

    override suspend fun marcarPagado(id: String, fechaPago: String, numeroSeguimiento: String?): Result<Unit> =
        runCatching {
            supabaseClient.postgrest["documentos"].update(
                DocumentoMarcarPagado(fechaPago = fechaPago, numeroSeguimiento = numeroSeguimiento)
            ) {
                filter { eq("id", id) }
            }
        }

    override suspend fun anularDocumento(id: String): Result<Unit> =
        runCatching {
            supabaseClient.postgrest["documentos"].update(mapOf("estado" to "anulado")) {
                filter { eq("id", id) }
            }
        }
}
