package com.nexo.empresas.data.remote.dte

import com.nexo.empresas.domain.model.dte.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class DteRemoteDataSource @Inject constructor(
    private val supabase: SupabaseClient,
    private val httpClient: HttpClient,        // ktor client inyectado desde AppModule
    private val supabaseUrl: String,           // BuildConfig.SUPABASE_URL
    private val supabaseAnonKey: String        // BuildConfig.SUPABASE_ANON_KEY
) {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    // ── Llamada genérica a Edge Function ──────────────────────────────────────

    private suspend inline fun <reified Req, reified Res> callFunction(
        functionName: String,
        body: Req
    ): Res {
        val response = httpClient.post("$supabaseUrl/functions/v1/$functionName") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $supabaseAnonKey")
            header("apikey", supabaseAnonKey)
            setBody(json.encodeToString(body))
        }
        return json.decodeFromString(response.body())
    }

    // ── Emitir DTE ─────────────────────────────────────────────────────────

    suspend fun emitirDte(request: EmitirDteRequest): EmitirDteResponse =
        callFunction("emitir-dte", request)

    // ── Consultar estado SII ────────────────────────────────────────────────

    suspend fun consultarEstadoDte(dteId: String): EstadoDteResponse {
        @kotlinx.serialization.Serializable
        data class Req(val dte_id: String)
        return callFunction("consultar-estado-dte", Req(dteId))
    }

    // ── Lookup RUT ──────────────────────────────────────────────────────────

    suspend fun lookupRut(rut: String): RutInfo {
        @kotlinx.serialization.Serializable
        data class Req(val rut: String)
        return callFunction("lookup-rut", Req(rut))
    }

    // ── Registrar certificado ───────────────────────────────────────────────

    suspend fun registrarCertificado(
        empresaId: String,
        pfxBase64: String,
        clavePfx: String
    ): Boolean {
        @kotlinx.serialization.Serializable
        data class Req(
            val empresa_id: String,
            val pfx_base64: String,
            val clave_pfx: String
        )
        @kotlinx.serialization.Serializable
        data class Res(val success: Boolean)
        val res: Res = callFunction("registrar-certificado", Req(empresaId, pfxBase64, clavePfx))
        return res.success
    }

    // ── Listar DTEs ─────────────────────────────────────────────────────────

    suspend fun listarDtes(
        empresaId: String,
        estadoFiltro: String? = null,
        limit: Int = 50,
        offset: Int = 0
    ): List<Dte> = supabase.postgrest["dtes"]
        .select {
            filter {
                eq("empresa_id", empresaId)
                estadoFiltro?.let { eq("estado_sii", it) }
            }
            order("created_at", Order.DESCENDING)
            range(offset.toLong(), (offset + limit - 1).toLong())
        }
        .decodeList()

    // ── Obtener DTE por ID ──────────────────────────────────────────────────

    suspend fun obtenerDte(dteId: String): Dte = supabase.postgrest["dtes"]
        .select {
            filter { eq("id", dteId) }
            limit(1)
        }
        .decodeSingle()

    // ── Items de un DTE ─────────────────────────────────────────────────────

    suspend fun obtenerItemsDte(dteId: String): List<ItemDte> =
        supabase.postgrest["items_dte"]
            .select { filter { eq("dte_id", dteId) } }
            .decodeList()

    // ── Folios ──────────────────────────────────────────────────────────────

    suspend fun listarFolios(empresaId: String): List<Folio> =
        supabase.postgrest["folios"]
            .select { filter { eq("empresa_id", empresaId) } }
            .decodeList()

    // ── URLs firmadas de Storage ────────────────────────────────────────────

    suspend fun descargarPdfUrl(pdfPath: String): String =
        supabase.storage["dte-pdfs"].createSignedUrl(pdfPath, 3600.seconds)

    suspend fun descargarXmlUrl(xmlPath: String): String =
        supabase.storage["dte-xmls"].createSignedUrl(xmlPath, 3600.seconds)
}
