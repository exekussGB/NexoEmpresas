package com.nexo.empresas.di

import com.nexo.empresas.core.session.TenantManager
import com.nexo.empresas.core.service.FcmTokenManager
import com.nexo.empresas.core.util.Constants
import com.nexo.empresas.data.remote.dte.DteRemoteDataSource
import com.nexo.empresas.dte.data.repository.DteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DteModule {

    @Provides
    @Singleton
    fun provideFcmTokenManager(
        supabase: SupabaseClient,
        tenantManager: TenantManager
    ): FcmTokenManager = FcmTokenManager(supabase, tenantManager)

    @Singleton
    @Provides
    fun provideHttpClient(): HttpClient {
        return HttpClient {
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

    @Singleton
    @Provides
    fun provideDteRemoteDataSource(
        httpClient: HttpClient
    ): DteRemoteDataSource {
        return DteRemoteDataSource(
            httpClient = httpClient,
            baseUrl = Constants.DTE_BASE_URL,
            apiKey = Constants.DTE_API_KEY
        )
    }

    @Singleton
    @Provides
    fun provideDteRepository(
        remoteDataSource: DteRemoteDataSource
    ): DteRepository {
        return DteRepository(
            remoteDataSource = remoteDataSource
        )
    }
}
