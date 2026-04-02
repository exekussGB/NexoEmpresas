package cl.nexo.empresas.presentation.settings;

import cl.nexo.empresas.core.session.SessionManager;
import cl.nexo.empresas.domain.repository.EmpresasRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.SupabaseClient;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<EmpresasRepository> empresasRepositoryProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<SupabaseClient> supabaseProvider;

  private SettingsViewModel_Factory(Provider<EmpresasRepository> empresasRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider, Provider<SupabaseClient> supabaseProvider) {
    this.empresasRepositoryProvider = empresasRepositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
    this.supabaseProvider = supabaseProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(empresasRepositoryProvider.get(), sessionManagerProvider.get(), supabaseProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<EmpresasRepository> empresasRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider, Provider<SupabaseClient> supabaseProvider) {
    return new SettingsViewModel_Factory(empresasRepositoryProvider, sessionManagerProvider, supabaseProvider);
  }

  public static SettingsViewModel newInstance(EmpresasRepository empresasRepository,
      SessionManager sessionManager, SupabaseClient supabase) {
    return new SettingsViewModel(empresasRepository, sessionManager, supabase);
  }
}
