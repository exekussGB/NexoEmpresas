package cl.nexo.empresas.core.tutorial;

import androidx.datastore.core.DataStore;
import androidx.datastore.preferences.core.Preferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("cl.nexo.empresas.core.tutorial.TutorialDataStore")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class TutorialManager_Factory implements Factory<TutorialManager> {
  private final Provider<DataStore<Preferences>> dataStoreProvider;

  private TutorialManager_Factory(Provider<DataStore<Preferences>> dataStoreProvider) {
    this.dataStoreProvider = dataStoreProvider;
  }

  @Override
  public TutorialManager get() {
    return newInstance(dataStoreProvider.get());
  }

  public static TutorialManager_Factory create(Provider<DataStore<Preferences>> dataStoreProvider) {
    return new TutorialManager_Factory(dataStoreProvider);
  }

  public static TutorialManager newInstance(DataStore<Preferences> dataStore) {
    return new TutorialManager(dataStore);
  }
}
