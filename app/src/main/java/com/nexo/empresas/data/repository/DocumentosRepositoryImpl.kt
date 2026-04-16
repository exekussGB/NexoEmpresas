package com.nexo.empresas.data.repository

import com.nexo.empresas.core.session.SessionManager
import com.nexo.empresas.data.model.*
import com.nexo.empresas.domain.repository.DocumentosRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import java.time.LocalDate
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
                    // Excluir registros de pago interno (tienen referencia a otro documento).
                    // Estos solo sirven para marcar el original como pagado, no son facturas/boletas.
                    filter("referencia_doc_id", FilterOperator.IS, "null")
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

            // 3. Si este documento es el pago/cobro de otro (referencia),
            //    marcar el documento original como pagado Y sus cheques como cobrados.
            val refId = doc.referenciaDocId
            if (refId != null) {
                // Marcar documento original como pagado
                runCatching {
                    supabaseClient.postgrest["documentos"].update(
                        DocumentoMarcarPagado(
                            estado             = "pagado",
                            fechaPago          = doc.fechaMovimiento,
                            numeroSeguimiento  = null
                        )
                    ) {
                        filter { eq("id", refId) }
                    }
                }
                // Marcar cheques asociados al documento original como cobrados
                runCatching {
                    supabaseClient.postgrest["cheques"].update(
                        ChequeEstadoUpdate("cobrado")
                    ) {
                        filter {
                            eq("documento_id", refId)
                            eq("estado", "pendiente")
                        }
                    }
                }
            }

            inserted.id
        }

    override suspend fun marcarPagado(id: String, fechaPago: String, numeroSeguimiento: String?): Result<Unit> =
        runCatching {
            // Marcar documento como pagado
            supabaseClient.postgrest["documentos"].update(
                DocumentoMarcarPagado(
                    estado            = "pagado",
                    fechaPago         = fechaPago,
                    numeroSeguimiento = numeroSeguimiento
                )
            ) {
                filter { eq("id", id) }
            }
            // También marcar los cheques asociados como cobrados
            runCatching {
                supabaseClient.postgrest["cheques"].update(
                    ChequeEstadoUpdate("cobrado")
                ) {
                    filter {
                        eq("documento_id", id)
                        eq("estado", "pendiente")
                    }
                }
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
