package com.nexo.empresas.core.tutorial;

import android.content.Context;
import androidx.datastore.core.DataStore;
import androidx.datastore.preferences.core.Preferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata({
    "com.nexo.empresas.core.tutorial.TutorialDataStore",
    "dagger.hilt.android.qualifiers.ApplicationContext"
})
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
public final class TutorialDiModule_ProvideTutorialDataStoreFactory implements Factory<DataStore<Preferences>> {
  private final Provider<Context> contextProvider;

  private TutorialDiModule_ProvideTutorialDataStoreFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DataStore<Preferences> get() {
    return provideTutorialDataStore(contextProvider.get());
  }

  public static TutorialDiModule_ProvideTutorialDataStoreFactory create(
      Provider<Context> contextProvider) {
    return new TutorialDiModule_ProvideTutorialDataStoreFactory(contextProvider);
  }

  public static DataStore<Preferences> provideTutorialDataStore(Context context) {
    return Preconditions.checkNotNullFromProvides(TutorialDiModule.INSTANCE.provideTutorialDataStore(context));
  }
}
