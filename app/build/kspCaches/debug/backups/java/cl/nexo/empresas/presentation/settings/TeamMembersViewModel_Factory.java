package cl.nexo.empresas.presentation.settings;

import cl.nexo.empresas.core.session.SessionManager;
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
public final class TeamMembersViewModel_Factory implements Factory<TeamMembersViewModel> {
  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<SupabaseClient> supabaseProvider;

  private TeamMembersViewModel_Factory(Provider<SessionManager> sessionManagerProvider,
      Provider<SupabaseClient> supabaseProvider) {
    this.sessionManagerProvider = sessionManagerProvider;
    this.supabaseProvider = supabaseProvider;
  }

  @Override
  public TeamMembersViewModel get() {
    return newInstance(sessionManagerProvider.get(), supabaseProvider.get());
  }

  public static TeamMembersViewModel_Factory create(Provider<SessionManager> sessionManagerProvider,
      Provider<SupabaseClient> supabaseProvider) {
    return new TeamMembersViewModel_Factory(sessionManagerProvider, supabaseProvider);
  }

  public static TeamMembersViewModel newInstance(SessionManager sessionManager,
      SupabaseClient supabase) {
    return new TeamMembersViewModel(sessionManager, supabase);
  }
}
