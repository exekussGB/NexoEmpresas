package com.nexo.empresas.dte.data.repository;

import com.nexo.empresas.data.remote.dte.DteRemoteDataSource;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class DteRepository_Factory implements Factory<DteRepository> {
  private final Provider<DteRemoteDataSource> remoteDataSourceProvider;

  private DteRepository_Factory(Provider<DteRemoteDataSource> remoteDataSourceProvider) {
    this.remoteDataSourceProvider = remoteDataSourceProvider;
  }

  @Override
  public DteRepository get() {
    return newInstance(remoteDataSourceProvider.get());
  }

  public static DteRepository_Factory create(
      Provider<DteRemoteDataSource> remoteDataSourceProvider) {
    return new DteRepository_Factory(remoteDataSourceProvider);
  }

  public static DteRepository newInstance(DteRemoteDataSource remoteDataSource) {
    return new DteRepository(remoteDataSource);
  }
}
