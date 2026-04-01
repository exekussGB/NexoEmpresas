package cl.nexo.empresas.core.session;

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
public final class SessionManager_Factory implements Factory<SessionManager> {
  private final Provider<SupabaseClient> supabaseProvider;

  private final Provider<TenantManager> tenantManagerProvider;

  private SessionManager_Factory(Provider<SupabaseClient> supabaseProvider,
      Provider<TenantManager> tenantManagerProvider) {
    this.supabaseProvider = supabaseProvider;
    this.tenantManagerProvider = tenantManagerProvider;
  }

  @Override
  public SessionManager get() {
    return newInstance(supabaseProvider.get(), tenantManagerProvider.get());
  }

  public static SessionManager_Factory create(Provider<SupabaseClient> supabaseProvider,
      Provider<TenantManager> tenantManagerProvider) {
    return new SessionManager_Factory(supabaseProvider, tenantManagerProvider);
  }

  public static SessionManager newInstance(SupabaseClient supabase, TenantManager tenantManager) {
    return new SessionManager(supabase, tenantManager);
  }
}
