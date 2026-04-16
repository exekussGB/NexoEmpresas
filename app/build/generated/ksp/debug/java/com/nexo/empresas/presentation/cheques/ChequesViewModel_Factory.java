package com.nexo.empresas.presentation.cheques;

import com.nexo.empresas.core.session.SessionManager;
import com.nexo.empresas.domain.repository.ChequesRepository;
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
public final class ChequesViewModel_Factory implements Factory<ChequesViewModel> {
  private final Provider<ChequesRepository> repositoryProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  private ChequesViewModel_Factory(Provider<ChequesRepository> repositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public ChequesViewModel get() {
    return newInstance(repositoryProvider.get(), sessionManagerProvider.get());
  }

  public static ChequesViewModel_Factory create(Provider<ChequesRepository> repositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new ChequesViewModel_Factory(repositoryProvider, sessionManagerProvider);
  }

  public static ChequesViewModel newInstance(ChequesRepository repository,
      SessionManager sessionManager) {
    return new ChequesViewModel(repository, sessionManager);
  }
}
