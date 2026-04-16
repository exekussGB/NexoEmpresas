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
public final class DocumentosRepositoryImpl_Factory implements Factory<DocumentosRepositoryImpl> {
  private final Provider<SupabaseClient> supabaseClientProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  private DocumentosRepositoryImpl_Factory(Provider<SupabaseClient> supabaseClientProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.supabaseClientProvider = supabaseClientProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public DocumentosRepositoryImpl get() {
    return newInstance(supabaseClientProvider.get(), sessionManagerProvider.get());
  }

  public static DocumentosRepositoryImpl_Factory create(
      Provider<SupabaseClient> supabaseClientProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new DocumentosRepositoryImpl_Factory(supabaseClientProvider, sessionManagerProvider);
  }

  public static DocumentosRepositoryImpl newInstance(SupabaseClient supabaseClient,
      SessionManager sessionManager) {
    return new DocumentosRepositoryImpl(supabaseClient, sessionManager);
  }
}
