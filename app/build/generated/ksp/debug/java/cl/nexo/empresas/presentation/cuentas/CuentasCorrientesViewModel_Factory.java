package cl.nexo.empresas.presentation.cuentas;

import cl.nexo.empresas.core.session.TenantManager;
import cl.nexo.empresas.domain.repository.CuentasCorrientesRepository;
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
public final class CuentasCorrientesViewModel_Factory implements Factory<CuentasCorrientesViewModel> {
  private final Provider<CuentasCorrientesRepository> repoProvider;

  private final Provider<TenantManager> tenantManagerProvider;

  private CuentasCorrientesViewModel_Factory(Provider<CuentasCorrientesRepository> repoProvider,
      Provider<TenantManager> tenantManagerProvider) {
    this.repoProvider = repoProvider;
    this.tenantManagerProvider = tenantManagerProvider;
  }

  @Override
  public CuentasCorrientesViewModel get() {
    return newInstance(repoProvider.get(), tenantManagerProvider.get());
  }

  public static CuentasCorrientesViewModel_Factory create(
      Provider<CuentasCorrientesRepository> repoProvider,
      Provider<TenantManager> tenantManagerProvider) {
    return new CuentasCorrientesViewModel_Factory(repoProvider, tenantManagerProvider);
  }

  public static CuentasCorrientesViewModel newInstance(CuentasCorrientesRepository repo,
      TenantManager tenantManager) {
    return new CuentasCorrientesViewModel(repo, tenantManager);
  }
}
