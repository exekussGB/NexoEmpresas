package com.nexo.empresas.core.di;

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
public final class DteModule_ProvideDteHttpClientFactory implements Factory<HttpClient> {
  @Override
  public HttpClient get() {
    return provideDteHttpClient();
  }

  public static DteModule_ProvideDteHttpClientFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static HttpClient provideDteHttpClient() {
    return Preconditions.checkNotNullFromProvides(DteModule.INSTANCE.provideDteHttpClient());
  }

  private static final class InstanceHolder {
    static final DteModule_ProvideDteHttpClientFactory INSTANCE = new DteModule_ProvideDteHttpClientFactory();
  }
}
