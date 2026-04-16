package com.nexo.empresas.data.remote.dte

import com.nexo.empresas.dte.data.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

/**
 * Data source remoto para operaciones de DTE
 * Realiza llamadas HTTP al backend DTE
 */
class DteRemoteDataSource @Inject constructor(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val apiKey: String
) {

    /**
     * Emite un nuevo DTE
     */
    suspend fun emitirDte(request: EmitirDteRequest): EmitirDteResponse {
        if (baseUrl.isEmpty() || apiKey.isEmpty()) {
            return EmitirDteResponse(
                success = false,
                error = "DTE no configurado. Agrega baseUrl y apiKey para usar esta función."
            )
        }

        return try {
            httpClient.post("$baseUrl/dte/emitir") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $apiKey")
                setBody(request)
            }.body<EmitirDteResponse>()
        } catch (e: Exception) {
            EmitirDteResponse(
                success = false,
                error = "Error al emitir DTE: ${e.message}"
            )
        }
    }

    /**
     * Obtiene el estado de un DTE en SII
     */
    suspend fun obtenerEstadoDte(dteId: String): EstadoDteResponse {
        if (baseUrl.isEmpty() || apiKey.isEmpty()) {
            throw Exception("DTE no configurado")
        }

        return try {
            httpClient.get("$baseUrl/dte/$dteId/estado") {
                header("Authorization", "Bearer $apiKey")
            }.body<EstadoDteResponse>()
        } catch (e: Exception) {
            throw Exception("Error al obtener estado: ${e.message}")
        }
    }

    /**
     * Obtiene información de un RUT (lookup)
     */
    suspend fun obtenerInfoRut(rut: String): RutInfo {
        if (baseUrl.isEmpty() || apiKey.isEmpty()) {
            throw Exception("DTE no configurado")
        }

        return try {
            httpClient.get("$baseUrl/dte/rut/$rut") {
                header("Authorization", "Bearer $apiKey")
            }.body<RutInfo>()
        } catch (e: Exception) {
            throw Exception("Error al buscar RUT: ${e.message}")
        }
    }

    /**
     * Obtiene un DTE específico
     */
    suspend fun obtenerDte(dteId: String): Dte {
        if (baseUrl.isEmpty() || apiKey.isEmpty()) {
            throw Exception("DTE no configurado")
        }

        return try {
            httpClient.get("$baseUrl/dte/$dteId") {
                header("Authorization", "Bearer $apiKey")
            }.body<Dte>()
        } catch (e: Exception) {
            throw Exception("Error al obtener DTE: ${e.message}")
        }
    }

    /**
     * Lista todos los DTEs de una empresa
     */
    suspend fun obtenerDtesPorEmpresa(empresaId: String): List<Dte> {
        if (baseUrl.isEmpty() || apiKey.isEmpty()) {
            throw Exception("DTE no configurado")
        }

        return try {
            httpClient.get("$baseUrl/dte/empresa/$empresaId") {
                header("Authorization", "Bearer $apiKey")
            }.body<List<Dte>>()
        } catch (e: Exception) {
            throw Exception("Error al listar DTEs: ${e.message}")
        }
    }

    /**
     * Obtiene los items de un DTE
     */
    suspend fun obtenerItemsDte(dteId: String): List<ItemDte> {
        if (baseUrl.isEmpty() || apiKey.isEmpty()) {
            throw Exception("DTE no configurado")
        }

        return try {
            httpClient.get("$baseUrl/dte/$dteId/items") {
                header("Authorization", "Bearer $apiKey")
            }.body<List<ItemDte>>()
        } catch (e: Exception) {
            throw Exception("Error al obtener items: ${e.message}")
        }
    }

    /**
     * Obtiene los folios disponibles para una empresa
     */
    suspend fun obtenerFolios(rutEmpresa: String): List<Folio> {
        if (baseUrl.isEmpty() || apiKey.isEmpty()) {
            throw Exception("DTE no configurado")
        }

        return try {
            httpClient.get("$baseUrl/dte/folios/$rutEmpresa") {
                header("Authorization", "Bearer $apiKey")
            }.body<List<Folio>>()
        } catch (e: Exception) {
            throw Exception("Error al obtener folios: ${e.message}")
        }
    }

    /**
     * Obtiene el folio actual para un tipo de DTE
     */
    suspend fun obtenerFolioActual(rutEmpresa: String, tipoDte: Int): Folio {
        if (baseUrl.isEmpty() || apiKey.isEmpty()) {
            throw Exception("DTE no configurado")
        }

        return try {
            httpClient.get("$baseUrl/dte/folios/$rutEmpresa/$tipoDte") {
                header("Authorization", "Bearer $apiKey")
            }.body<Folio>()
        } catch (e: Exception) {
            throw Exception("Error al obtener folio: ${e.message}")
        }
    }
}
