package com.nexo.empresas.data.network;

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
public final class IndicadoresService_Factory implements Factory<IndicadoresService> {
  private final Provider<HttpClient> httpClientProvider;

  private IndicadoresService_Factory(Provider<HttpClient> httpClientProvider) {
    this.httpClientProvider = httpClientProvider;
  }

  @Override
  public IndicadoresService get() {
    return newInstance(httpClientProvider.get());
  }

  public static IndicadoresService_Factory create(Provider<HttpClient> httpClientProvider) {
    return new IndicadoresService_Factory(httpClientProvider);
  }

  public static IndicadoresService newInstance(HttpClient httpClient) {
    return new IndicadoresService(httpClient);
  }
}
