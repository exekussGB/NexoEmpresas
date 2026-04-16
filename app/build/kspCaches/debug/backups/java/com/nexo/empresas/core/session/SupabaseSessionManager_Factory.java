package com.nexo.empresas.core.session;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class SupabaseSessionManager_Factory implements Factory<SupabaseSessionManager> {
  private final Provider<Context> contextProvider;

  private SupabaseSessionManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SupabaseSessionManager get() {
    return newInstance(contextProvider.get());
  }

  public static SupabaseSessionManager_Factory create(Provider<Context> contextProvider) {
    return new SupabaseSessionManager_Factory(contextProvider);
  }

  public static SupabaseSessionManager newInstance(Context context) {
    return new SupabaseSessionManager(context);
  }
}
