package com.nexo.empresas.data.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class IndicadoresResponse(
    val utm: IndicadorValue
)

@Serializable
data class IndicadorValue(
    val valor: Double
)

class IndicadoresService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    suspend fun fetchUtm(): Double? {
        return try {
            val response: IndicadoresResponse = client.get("https://mindicador.cl/api").body()
            response.utm.valor
        } catch (e: Exception) {
            null
        }
    }
}
