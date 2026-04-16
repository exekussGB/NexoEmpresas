package com.nexo.empresas.data.remote.dte;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.ktor.client.HttpClient;
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
public final class DteRemoteDataSource_Factory implements Factory<DteRemoteDataSource> {
  private final Provider<HttpClient> httpClientProvider;

  private final Provider<String> baseUrlProvider;

  private final Provider<String> apiKeyProvider;

  private DteRemoteDataSource_Factory(Provider<HttpClient> httpClientProvider,
      Provider<String> baseUrlProvider, Provider<String> apiKeyProvider) {
    this.httpClientProvider = httpClientProvider;
    this.baseUrlProvider = baseUrlProvider;
    this.apiKeyProvider = apiKeyProvider;
  }

  @Override
  public DteRemoteDataSource get() {
    return newInstance(httpClientProvider.get(), baseUrlProvider.get(), apiKeyProvider.get());
  }

  public static DteRemoteDataSource_Factory create(Provider<HttpClient> httpClientProvider,
      Provider<String> baseUrlProvider, Provider<String> apiKeyProvider) {
    return new DteRemoteDataSource_Factory(httpClientProvider, baseUrlProvider, apiKeyProvider);
  }

  public static DteRemoteDataSource newInstance(HttpClient httpClient, String baseUrl,
      String apiKey) {
    return new DteRemoteDataSource(httpClient, baseUrl, apiKey);
  }
}
