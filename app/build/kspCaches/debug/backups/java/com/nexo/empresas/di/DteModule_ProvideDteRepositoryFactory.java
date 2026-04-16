package com.nexo.empresas.di;

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
  private final Provider<DteRemoteDataSource> remoteDataSourceProvider;

  private DteModule_ProvideDteRepositoryFactory(
      Provider<DteRemoteDataSource> remoteDataSourceProvider) {
    this.remoteDataSourceProvider = remoteDataSourceProvider;
  }

  @Override
  public DteRepository get() {
    return provideDteRepository(remoteDataSourceProvider.get());
  }

  public static DteModule_ProvideDteRepositoryFactory create(
      Provider<DteRemoteDataSource> remoteDataSourceProvider) {
    return new DteModule_ProvideDteRepositoryFactory(remoteDataSourceProvider);
  }

  public static DteRepository provideDteRepository(DteRemoteDataSource remoteDataSource) {
    return Preconditions.checkNotNullFromProvides(DteModule.INSTANCE.provideDteRepository(remoteDataSource));
  }
}
