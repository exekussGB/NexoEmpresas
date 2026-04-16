package com.nexo.empresas.core.di

import android.content.Context
import com.nexo.empresas.core.session.SupabaseSessionManager
import com.nexo.empresas.core.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

/**
 * Módulo Hilt que provee el cliente Supabase único para toda la app.
 *
 * Cambios respecto a la versión anterior:
 *   1. [sessionManager] apunta a [SupabaseSessionManager] (DataStore-backed).
 *      Antes no existía esta clase → el SDK usaba memoria volátil.
 *   2. [alwaysAutoRefresh = true]: el SDK renueva el access_token en segundo
 *      plano antes de que expire (tokens Supabase duran 1 h por defecto).
 *      El usuario nunca vuelve a ver "sesión expirada".
 *   3. [autoLoadFromStorage = true]: al arrancar la app, el SDK carga
 *      automáticamente la sesión guardada, restaurando al usuario sin login.
 */
@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseSessionManager(
        @ApplicationContext context: Context
    ): SupabaseSessionManager = SupabaseSessionManager(context)

    @Provides
    @Singleton
    fun provideSupabaseClient(
        @ApplicationContext context: Context,
        sessionManager: SupabaseSessionManager
    ): SupabaseClient =
        createSupabaseClient(
            supabaseUrl = Constants.SUPABASE_URL,
            supabaseKey = Constants.SUPABASE_ANON_KEY
        ) {
            install(Auth) {
                this.sessionManager = sessionManager   // ← Persistencia real
                alwaysAutoRefresh = true               // ← Refresh automático
                autoLoadFromStorage = true             // ← Restaura sesión al iniciar
            }
            install(Postgrest)
            install(Storage)
            install(Realtime)
        }
}
