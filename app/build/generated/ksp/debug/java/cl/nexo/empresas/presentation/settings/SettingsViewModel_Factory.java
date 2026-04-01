package cl.nexo.empresas.presentation.settings;

import cl.nexo.empresas.core.session.SessionManager;
import cl.nexo.empresas.domain.repository.EmpresasRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
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

  private SettingsViewModel_Factory(Provider<EmpresasRepository> empresasRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.empresasRepositoryProvider = empresasRepositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(empresasRepositoryProvider.get(), sessionManagerProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<EmpresasRepository> empresasRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new SettingsViewModel_Factory(empresasRepositoryProvider, sessionManagerProvider);
  }

  public static SettingsViewModel newInstance(EmpresasRepository empresasRepository,
      SessionManager sessionManager) {
    return new SettingsViewModel(empresasRepository, sessionManager);
  }
}
