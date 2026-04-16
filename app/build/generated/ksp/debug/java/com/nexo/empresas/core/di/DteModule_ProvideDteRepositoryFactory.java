package com.nexo.empresas.core.di;

import com.nexo.empresas.data.remote.dte.DteRemoteDataSource;
import com.nexo.empresas.dte.data.repository.DteRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
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
public final class DteModule_ProvideDteRepositoryFactory implements Factory<DteRepository> {
  private final Provider<DteRemoteDataSource> remoteProvider;

  private DteModule_ProvideDteRepositoryFactory(Provider<DteRemoteDataSource> remoteProvider) {
    this.remoteProvider = remoteProvider;
  }

  @Override
  public DteRepository get() {
    return provideDteRepository(remoteProvider.get());
  }

  public static DteModule_ProvideDteRepositoryFactory create(
      Provider<DteRemoteDataSource> remoteProvider) {
    return new DteModule_ProvideDteRepositoryFactory(remoteProvider);
  }

  public static DteRepository provideDteRepository(DteRemoteDataSource remote) {
    return Preconditions.checkNotNullFromProvides(DteModule.INSTANCE.provideDteRepository(remote));
  }
}
