package com.nexo.empresas.data.remote.dte;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class DteRemoteDataSource_Factory implements Factory<DteRemoteDataSource> {
  private final Provider<SupabaseClient> supabaseProvider;

  private final Provider<HttpClient> httpClientProvider;

  private final Provider<String> supabaseUrlProvider;

  private final Provider<String> supabaseAnonKeyProvider;

  private DteRemoteDataSource_Factory(Provider<SupabaseClient> supabaseProvider,
      Provider<HttpClient> httpClientProvider, Provider<String> supabaseUrlProvider,
      Provider<String> supabaseAnonKeyProvider) {
    this.supabaseProvider = supabaseProvider;
    this.httpClientProvider = httpClientProvider;
    this.supabaseUrlProvider = supabaseUrlProvider;
    this.supabaseAnonKeyProvider = supabaseAnonKeyProvider;
  }

  @Override
  public DteRemoteDataSource get() {
    return newInstance(supabaseProvider.get(), httpClientProvider.get(), supabaseUrlProvider.get(), supabaseAnonKeyProvider.get());
  }

  public static DteRemoteDataSource_Factory create(Provider<SupabaseClient> supabaseProvider,
      Provider<HttpClient> httpClientProvider, Provider<String> supabaseUrlProvider,
      Provider<String> supabaseAnonKeyProvider) {
    return new DteRemoteDataSource_Factory(supabaseProvider, httpClientProvider, supabaseUrlProvider, supabaseAnonKeyProvider);
  }

  public static DteRemoteDataSource newInstance(SupabaseClient supabase, HttpClient httpClient,
      String supabaseUrl, String supabaseAnonKey) {
    return new DteRemoteDataSource(supabase, httpClient, supabaseUrl, supabaseAnonKey);
  }
}
