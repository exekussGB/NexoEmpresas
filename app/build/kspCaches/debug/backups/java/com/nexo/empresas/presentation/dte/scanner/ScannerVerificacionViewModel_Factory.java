package com.nexo.empresas.presentation.dte.scanner;

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
public final class ScannerVerificacionViewModel_Factory implements Factory<ScannerVerificacionViewModel> {
  private final Provider<HttpClient> httpClientProvider;

  private ScannerVerificacionViewModel_Factory(Provider<HttpClient> httpClientProvider) {
    this.httpClientProvider = httpClientProvider;
  }

  @Override
  public ScannerVerificacionViewModel get() {
    return newInstance(httpClientProvider.get());
  }

  public static ScannerVerificacionViewModel_Factory create(
      Provider<HttpClient> httpClientProvider) {
    return new ScannerVerificacionViewModel_Factory(httpClientProvider);
  }

  public static ScannerVerificacionViewModel newInstance(HttpClient httpClient) {
    return new ScannerVerificacionViewModel(httpClient);
  }
}
