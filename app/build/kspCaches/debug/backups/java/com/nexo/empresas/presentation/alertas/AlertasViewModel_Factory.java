package com.nexo.empresas.presentation.alertas;

import com.nexo.empresas.core.session.SessionManager;
import com.nexo.empresas.domain.repository.AlertasRepository;
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
public final class AlertasViewModel_Factory implements Factory<AlertasViewModel> {
  private final Provider<AlertasRepository> alertasRepositoryProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  private AlertasViewModel_Factory(Provider<AlertasRepository> alertasRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.alertasRepositoryProvider = alertasRepositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public AlertasViewModel get() {
    return newInstance(alertasRepositoryProvider.get(), sessionManagerProvider.get());
  }

  public static AlertasViewModel_Factory create(
      Provider<AlertasRepository> alertasRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new AlertasViewModel_Factory(alertasRepositoryProvider, sessionManagerProvider);
  }

  public static AlertasViewModel newInstance(AlertasRepository alertasRepository,
      SessionManager sessionManager) {
    return new AlertasViewModel(alertasRepository, sessionManager);
  }
}
