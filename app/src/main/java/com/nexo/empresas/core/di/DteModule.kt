package com.nexo.empresas.core.di

import com.nexo.empresas.BuildConfig
import com.nexo.empresas.data.remote.dte.DteRemoteDataSource
import com.nexo.empresas.dte.data.repository.DteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DteModule {
    @Provides
    @Singleton
    fun provideDteHttpClient(): HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; coerceInputValues = true })
        }
        defaultRequest {
            // headers comunes.
        }
    }

    @Provides
    @Singleton
    fun provideDteRemoteDataSource(
        supabase: SupabaseClient,
        httpClient: HttpClient
    ): DteRemoteDataSource = DteRemoteDataSource(
        supabase        = supabase,
        httpClient      = httpClient,
        supabaseUrl     = BuildConfig.SUPABASE_URL,
        supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY
    )

    @Provides
    @Singleton
    fun provideDteRepository(
        remote: DteRemoteDataSource
    ): DteRepository = DteRepository(remote)
}
