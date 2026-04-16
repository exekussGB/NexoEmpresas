package com.nexo.empresas.core.di;

import com.nexo.empresas.data.remote.dte.DteRemoteDataSource;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.SupabaseClient;
import io.ktor.client.HttpClient;
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
public final class DteModule_ProvideDteRemoteDataSourceFactory implements Factory<DteRemoteDataSource> {
  private final Provider<SupabaseClient> supabaseProvider;

  private final Provider<HttpClient> httpClientProvider;

  private DteModule_ProvideDteRemoteDataSourceFactory(Provider<SupabaseClient> supabaseProvider,
      Provider<HttpClient> httpClientProvider) {
    this.supabaseProvider = supabaseProvider;
    this.httpClientProvider = httpClientProvider;
  }

  @Override
  public DteRemoteDataSource get() {
    return provideDteRemoteDataSource(supabaseProvider.get(), httpClientProvider.get());
  }

  public static DteModule_ProvideDteRemoteDataSourceFactory create(
      Provider<SupabaseClient> supabaseProvider, Provider<HttpClient> httpClientProvider) {
    return new DteModule_ProvideDteRemoteDataSourceFactory(supabaseProvider, httpClientProvider);
  }

  public static DteRemoteDataSource provideDteRemoteDataSource(SupabaseClient supabase,
      HttpClient httpClient) {
    return Preconditions.checkNotNullFromProvides(DteModule.INSTANCE.provideDteRemoteDataSource(supabase, httpClient));
  }
}
