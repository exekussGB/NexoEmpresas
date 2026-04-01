package cl.nexo.empresas.core.tutorial

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TutorialManager @Inject constructor(
    @TutorialDataStore private val dataStore: DataStore<Preferences>
) {
    suspend fun isTutorialCompleted(module: TutorialModule): Boolean {
        val key = booleanPreferencesKey(module.key)
        return dataStore.data.map { it[key] ?: false }.first()
    }

    suspend fun markTutorialCompleted(module: TutorialModule) {
        val key = booleanPreferencesKey(module.key)
        dataStore.edit { it[key] = true }
    }

    suspend fun resetTutorial(module: TutorialModule) {
        val key = booleanPreferencesKey(module.key)
        dataStore.edit { it.remove(key) }
    }

    suspend fun resetAllTutorials() {
        dataStore.edit { prefs ->
            TutorialModule.entries.forEach { module ->
                prefs.remove(booleanPreferencesKey(module.key))
            }
        }
    }

    suspend fun getAllStatus(): Map<TutorialModule, Boolean> {
        val prefs = dataStore.data.first()
        return TutorialModule.entries.associateWith { module ->
            prefs[booleanPreferencesKey(module.key)] ?: false
        }
    }
}
