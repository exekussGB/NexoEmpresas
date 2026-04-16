package com.nexo.empresas.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.SupabaseClient;
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
public final class GraficosRepositoryImpl_Factory implements Factory<GraficosRepositoryImpl> {
  private final Provider<SupabaseClient> clientProvider;

  private GraficosRepositoryImpl_Factory(Provider<SupabaseClient> clientProvider) {
    this.clientProvider = clientProvider;
  }

  @Override
  public GraficosRepositoryImpl get() {
    return newInstance(clientProvider.get());
  }

  public static GraficosRepositoryImpl_Factory create(Provider<SupabaseClient> clientProvider) {
    return new GraficosRepositoryImpl_Factory(clientProvider);
  }

  public static GraficosRepositoryImpl newInstance(SupabaseClient client) {
    return new GraficosRepositoryImpl(client);
  }
}
