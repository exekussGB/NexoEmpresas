package com.nexo.empresas.data.remote.dte

import com.nexo.empresas.data.model.Dte
import com.nexo.empresas.data.model.EmitirDteRequest
import com.nexo.empresas.data.model.EmitirDteResponse
import com.nexo.empresas.data.model.EstadoDteResponse
import com.nexo.empresas.data.model.Folio
import com.nexo.empresas.data.model.ItemDte
import com.nexo.empresas.data.model.RutInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

class DteRemoteDataSource(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val apiKey: String
) {

    /**
     * Emite un nuevo DTE
     */
    suspend fun emitirDte(request: EmitirDteRequest): Result<EmitirDteResponse> = runCatching {
        httpClient.post<EmitirDteResponse>(
            urlString = "$baseUrl/dte/emitir"
        ) {
            contentType(ContentType.Application.Json)
            setBody(request)
            headers.append("Authorization", "Bearer $apiKey")
        }
    }

    /**
     * Obtiene el estado de un DTE
     */
    suspend fun obtenerEstadoDte(dteId: String): Result<EstadoDteResponse> = runCatching {
        httpClient.get<EstadoDteResponse>(
            urlString = "$baseUrl/dte/$dteId/estado"
        ) {
            headers.append("Authorization", "Bearer $apiKey")
        }
    }

    /**
     * Obtiene información del RUT
     */
    suspend fun obtenerInfoRut(rut: String): Result<RutInfo> = runCatching {
        httpClient.get<RutInfo>(
            urlString = "$baseUrl/rut/$rut"
        ) {
            headers.append("Authorization", "Bearer $apiKey")
        }
    }

    /**
     * Obtiene los folios disponibles para una empresa
     */
    suspend fun obtenerFolios(rutEmpresa: String): Result<List<Folio>> = runCatching {
        httpClient.get<List<Folio>>(
            urlString = "$baseUrl/empresa/$rutEmpresa/folios"
        ) {
            headers.append("Authorization", "Bearer $apiKey")
        }
    }

    /**
     * Obtiene los DTEs de una empresa
     */
    suspend fun obtenerDtesPorEmpresa(rutEmpresa: String): Result<List<Dte>> = runCatching {
        httpClient.get<List<Dte>>(
            urlString = "$baseUrl/empresa/$rutEmpresa/dtes"
        ) {
            headers.append("Authorization", "Bearer $apiKey")
        }
    }

    /**
     * Obtiene un DTE específico
     */
    suspend fun obtenerDte(dteId: String): Result<Dte> = runCatching {
        httpClient.get<Dte>(
            urlString = "$baseUrl/dte/$dteId"
        ) {
            headers.append("Authorization", "Bearer $apiKey")
        }
    }

    /**
     * Obtiene los items de un DTE
     */
    suspend fun obtenerItemsDte(dteId: String): Result<List<ItemDte>> = runCatching {
        httpClient.get<List<ItemDte>>(
            urlString = "$baseUrl/dte/$dteId/items"
        ) {
            headers.append("Authorization", "Bearer $apiKey")
        }
    }

    /**
     * Obtiene el folio actual para un tipo de DTE
     */
    suspend fun obtenerFolioActual(rutEmpresa: String, tipoDte: Int): Result<Folio> = runCatching {
        httpClient.get<Folio>(
            urlString = "$baseUrl/empresa/$rutEmpresa/folio/$tipoDte"
        ) {
            headers.append("Authorization", "Bearer $apiKey")
        }
    }
}
