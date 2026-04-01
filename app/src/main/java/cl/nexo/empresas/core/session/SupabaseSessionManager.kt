package cl.nexo.empresas.core.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación persistente del SessionManager del SDK de Supabase.
 *
 * Problema resuelto:
 *   Sin esta clase, el SDK usaba MemorySessionManager (en RAM).
 *   Al morir el proceso o expirar el token (1 h), el usuario era forzado
 *   a re-autenticarse aunque tuviera refresh_token válido.
 *
 * Solución:
 *   - Persiste la UserSession serializada en DataStore (cifrado en reposo
 *     por el sistema operativo en /data/data/.../files/datastore/).
 *   - El SDK carga la sesión al iniciar → restaura access_token + refresh_token.
 *   - Con [alwaysAutoRefresh = true] en SupabaseModule, el SDK renueva el
 *     access_token automáticamente antes de que expire, sin intervención del usuario.
 *
 * NOTA: DataStore usa `preferencesDataStore` delegado a nivel de archivo.
 *       El nombre "nexo_empresas_session" es fijo; no cambiarlo sin migración.
 */
private val Context.sessionDataStore by preferencesDataStore(name = "nexo_empresas_session")

@Singleton
class SupabaseSessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) : SessionManager {

    private companion object {
        val SESSION_KEY = stringPreferencesKey("supabase_user_session")

        // Json con ignoreUnknownKeys por si el SDK agrega nuevos campos
        // en versiones futuras sin romper la deserialización.
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = true
        }
    }

    override suspend fun saveSession(session: UserSession) {
        val serialized = json.encodeToString(session)
        context.sessionDataStore.edit { prefs ->
            prefs[SESSION_KEY] = serialized
        }
    }

    override suspend fun loadSession(): UserSession? {
        val prefs = context.sessionDataStore.data.firstOrNull() ?: return null
        val serialized = prefs[SESSION_KEY] ?: return null
        return runCatching {
            json.decodeFromString<UserSession>(serialized)
        }.getOrNull()
    }

    override suspend fun deleteSession() {
        context.sessionDataStore.edit { prefs ->
            prefs.remove(SESSION_KEY)
        }
    }
}
