package com.nexo.empresas.presentation.graficos;

import com.nexo.empresas.core.session.SessionManager;
import com.nexo.empresas.core.session.TenantManager;
import com.nexo.empresas.domain.repository.GraficosRepository;
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
public final class GraficosViewModel_Factory implements Factory<GraficosViewModel> {
  private final Provider<GraficosRepository> repoProvider;

  private final Provider<TenantManager> tenantManagerProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  private GraficosViewModel_Factory(Provider<GraficosRepository> repoProvider,
      Provider<TenantManager> tenantManagerProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.repoProvider = repoProvider;
    this.tenantManagerProvider = tenantManagerProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public GraficosViewModel get() {
    return newInstance(repoProvider.get(), tenantManagerProvider.get(), sessionManagerProvider.get());
  }

  public static GraficosViewModel_Factory create(Provider<GraficosRepository> repoProvider,
      Provider<TenantManager> tenantManagerProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new GraficosViewModel_Factory(repoProvider, tenantManagerProvider, sessionManagerProvider);
  }

  public static GraficosViewModel newInstance(GraficosRepository repo, TenantManager tenantManager,
      SessionManager sessionManager) {
    return new GraficosViewModel(repo, tenantManager, sessionManager);
  }
}
