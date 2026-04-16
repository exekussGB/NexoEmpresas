package com.nexo.empresas.core.di

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.supabaseSessionDataStore by preferencesDataStore(name = "supabase_session")

/**
 * Persiste la sesión de Supabase en DataStore para que sobreviva reinicios de la app.
 * Esto evita que el SDK caiga al anon key cuando no hay sesión en memoria.
 */
class SupabaseSessionManager(private val context: Context) : SessionManager {

    private val json = Json { ignoreUnknownKeys = true }
    private val SESSION_KEY = stringPreferencesKey("session")

    override suspend fun loadSession(): UserSession? {
        return try {
            val prefs = context.supabaseSessionDataStore.data.first()
            val sessionString = prefs[SESSION_KEY] ?: return null
            json.decodeFromString<UserSession>(sessionString)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun saveSession(session: UserSession) {
        try {
            context.supabaseSessionDataStore.edit { prefs ->
                prefs[SESSION_KEY] = json.encodeToString(session)
            }
        } catch (e: Exception) {
            // Ignorar errores al guardar
        }
    }

    override suspend fun deleteSession() {
        try {
            context.supabaseSessionDataStore.edit { prefs ->
                prefs.remove(SESSION_KEY)
            }
        } catch (e: Exception) {
            // Ignorar errores al borrar
        }
    }
}
