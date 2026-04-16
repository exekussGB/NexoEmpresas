package com.nexo.empresas.core.di;

import android.content.Context;
import com.nexo.empresas.core.session.SupabaseSessionManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class SupabaseModule_ProvideSupabaseSessionManagerFactory implements Factory<SupabaseSessionManager> {
  private final Provider<Context> contextProvider;

  private SupabaseModule_ProvideSupabaseSessionManagerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SupabaseSessionManager get() {
    return provideSupabaseSessionManager(contextProvider.get());
  }

  public static SupabaseModule_ProvideSupabaseSessionManagerFactory create(
      Provider<Context> contextProvider) {
    return new SupabaseModule_ProvideSupabaseSessionManagerFactory(contextProvider);
  }

  public static SupabaseSessionManager provideSupabaseSessionManager(Context context) {
    return Preconditions.checkNotNullFromProvides(SupabaseModule.INSTANCE.provideSupabaseSessionManager(context));
  }
}
