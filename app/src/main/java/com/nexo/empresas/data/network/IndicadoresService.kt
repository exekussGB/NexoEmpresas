package com.nexo.empresas.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import kotlin.math.roundToLong

class IndicadoresService @Inject constructor(
    private val httpClient: HttpClient
) {
    suspend fun fetchUtm(): Long? {
        return try {
            val response = httpClient.get("https://mindicador.cl/api")
            val json = response.body<JsonObject>()
            json["utm"]?.jsonObject?.get("valor")
                ?.jsonPrimitive?.double?.roundToLong()
        } catch (e: Exception) {
            null
        }
    }
}
