package com.nexo.empresas.data.repository;

import com.nexo.empresas.core.session.SessionManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.SupabaseClient;
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
public final class AlertasRepositoryImpl_Factory implements Factory<AlertasRepositoryImpl> {
  private final Provider<SupabaseClient> supabaseProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  private AlertasRepositoryImpl_Factory(Provider<SupabaseClient> supabaseProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.supabaseProvider = supabaseProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public AlertasRepositoryImpl get() {
    return newInstance(supabaseProvider.get(), sessionManagerProvider.get());
  }

  public static AlertasRepositoryImpl_Factory create(Provider<SupabaseClient> supabaseProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new AlertasRepositoryImpl_Factory(supabaseProvider, sessionManagerProvider);
  }

  public static AlertasRepositoryImpl newInstance(SupabaseClient supabase,
      SessionManager sessionManager) {
    return new AlertasRepositoryImpl(supabase, sessionManager);
  }
}
