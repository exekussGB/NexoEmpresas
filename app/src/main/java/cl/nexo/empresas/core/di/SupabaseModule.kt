package cl.nexo.empresas.core.di

import android.content.Context
import cl.nexo.empresas.core.util.Constants
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

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(@ApplicationContext context: Context): SupabaseClient =
        createSupabaseClient(
            supabaseUrl = Constants.SUPABASE_URL,
            supabaseKey = Constants.SUPABASE_ANON_KEY
        ) {
            install(Auth) {
                // Persiste la sesión en DataStore → resiste reinicios de app
                sessionManager = SupabaseSessionManager(context)
                // Refresca el token automáticamente antes de que expire
                alwaysAutoRefresh = true
                // Carga la sesión guardada al iniciar
                autoLoadFromStorage = true
            }
            install(Postgrest)
            install(Storage)
            install(Realtime)
        }
}
