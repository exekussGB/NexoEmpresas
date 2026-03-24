package cl.nexo.empresas.data.repository

import cl.nexo.empresas.core.session.SessionManager
import cl.nexo.empresas.data.model.*
import cl.nexo.empresas.domain.repository.DocumentosRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentosRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val sessionManager: SessionManager
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
            }.decodeList()
        }

    override suspend fun getDocumento(id: String): Result<Documento> =
        runCatching {
            supabaseClient.postgrest["documentos"].select {
                filter { eq("id", id) }
                limit(1)
                single()
            }.decodeSingle()
        }

    override suspend fun addDocumento(doc: DocumentoCreate, cheques: List<ChequeCreate>): Result<String> =
        runCatching {
            // 1. Insertar documento
            val inserted = supabaseClient.postgrest["documentos"].insert(doc) {
                select()
            }.decodeSingle<Documento>()

            // 2. Insertar cheques si corresponde (con rollback si falla)
            if (cheques.isNotEmpty()) {
                try {
                    val chequesConId = cheques.map { it.copy(documentoId = inserted.id) }
                    supabaseClient.postgrest["cheques"].insert(chequesConId)
                } catch (e: Exception) {
                    // Rollback: eliminar el documento ya insertado para mantener consistencia
                    runCatching {
                        supabaseClient.postgrest["documentos"].delete {
                            filter { eq("id", inserted.id) }
                        }
                    }
                    throw e
                }
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

    override suspend fun getDocumentosVencimientoProximo(tipo: String, fechaLimite: String): Result<List<Documento>> =
        runCatching {
            val tipoFiltro = if (tipo == "cobro") "ingreso" else "egreso"
            supabaseClient.postgrest["documentos"].select {
                filter {
                    eq("empresa_id", sessionManager.empresaId)
                    eq("tipo", tipoFiltro)
                    eq("estado", "pendiente")
                    lte("fecha_vencimiento", fechaLimite)
                }
                order("fecha_vencimiento", Order.ASCENDING)
            }.decodeList()
        }
}
