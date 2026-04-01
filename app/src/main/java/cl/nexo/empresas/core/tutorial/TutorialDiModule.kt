package cl.nexo.empresas.core.tutorial

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TutorialDataStore

private val Context.tutorialDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "nexoempresas_tutorials"
)

@Module
@InstallIn(SingletonComponent::class)
object TutorialDiModule {

    @Provides
    @Singleton
    @TutorialDataStore
    fun provideTutorialDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.tutorialDataStore
}
