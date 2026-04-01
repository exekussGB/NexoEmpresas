package cl.nexo.empresas.presentation.auth;

import cl.nexo.empresas.domain.repository.AuthRepository;
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
public final class AuthViewModel_Factory implements Factory<AuthViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<SupabaseClient> supabaseClientProvider;

  private AuthViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<SupabaseClient> supabaseClientProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.supabaseClientProvider = supabaseClientProvider;
  }

  @Override
  public AuthViewModel get() {
    return newInstance(authRepositoryProvider.get(), supabaseClientProvider.get());
  }

  public static AuthViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<SupabaseClient> supabaseClientProvider) {
    return new AuthViewModel_Factory(authRepositoryProvider, supabaseClientProvider);
  }

  public static AuthViewModel newInstance(AuthRepository authRepository,
      SupabaseClient supabaseClient) {
    return new AuthViewModel(authRepository, supabaseClient);
  }
}
