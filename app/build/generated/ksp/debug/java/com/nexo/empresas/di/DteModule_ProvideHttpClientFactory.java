package com.nexo.empresas.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
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
public final class DteModule_ProvideHttpClientFactory implements Factory<HttpClient> {
  @Override
  public HttpClient get() {
    return provideHttpClient();
  }

  public static DteModule_ProvideHttpClientFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static HttpClient provideHttpClient() {
    return Preconditions.checkNotNullFromProvides(DteModule.INSTANCE.provideHttpClient());
  }

  private static final class InstanceHolder {
    static final DteModule_ProvideHttpClientFactory INSTANCE = new DteModule_ProvideHttpClientFactory();
  }
}
