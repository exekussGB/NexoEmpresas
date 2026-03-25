package cl.nexo.empresas.data.repository;

import cl.nexo.empresas.core.session.SessionManager;
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
public final class DashboardRepositoryImpl_Factory implements Factory<DashboardRepositoryImpl> {
  private final Provider<SupabaseClient> supabaseProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  private DashboardRepositoryImpl_Factory(Provider<SupabaseClient> supabaseProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.supabaseProvider = supabaseProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public DashboardRepositoryImpl get() {
    return newInstance(supabaseProvider.get(), sessionManagerProvider.get());
  }

  public static DashboardRepositoryImpl_Factory create(Provider<SupabaseClient> supabaseProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new DashboardRepositoryImpl_Factory(supabaseProvider, sessionManagerProvider);
  }

  public static DashboardRepositoryImpl newInstance(SupabaseClient supabase,
      SessionManager sessionManager) {
    return new DashboardRepositoryImpl(supabase, sessionManager);
  }
}
