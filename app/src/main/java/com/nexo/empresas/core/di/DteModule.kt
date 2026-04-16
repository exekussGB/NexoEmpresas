package com.nexo.empresas.di

import com.nexo.empresas.core.util.Constants
import com.nexo.empresas.data.remote.dte.DteRemoteDataSource
import com.nexo.empresas.data.repository.DteRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val dteModule = module {
    // HttpClient para DTE API
    single<HttpClient> {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(Logging) {
                level = LogLevel.BODY
            }
        }
    }

    // Remote Data Source
    single {
        DteRemoteDataSource(
            httpClient = get(),
            baseUrl = Constants.DTE_BASE_URL,
            apiKey = Constants.DTE_API_KEY
        )
    }

    // Repository
    single {
        DteRepository(
            remoteDataSource = get()
        )
    }
}
