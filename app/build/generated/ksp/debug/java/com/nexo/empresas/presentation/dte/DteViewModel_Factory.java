package com.nexo.empresas.presentation.dte;

import com.nexo.empresas.core.session.TenantManager;
import com.nexo.empresas.dte.data.repository.DteRepository;
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
public final class DteViewModel_Factory implements Factory<DteViewModel> {
  private final Provider<DteRepository> repositoryProvider;

  private final Provider<TenantManager> tenantManagerProvider;

  private DteViewModel_Factory(Provider<DteRepository> repositoryProvider,
      Provider<TenantManager> tenantManagerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.tenantManagerProvider = tenantManagerProvider;
  }

  @Override
  public DteViewModel get() {
    return newInstance(repositoryProvider.get(), tenantManagerProvider.get());
  }

  public static DteViewModel_Factory create(Provider<DteRepository> repositoryProvider,
      Provider<TenantManager> tenantManagerProvider) {
    return new DteViewModel_Factory(repositoryProvider, tenantManagerProvider);
  }

  public static DteViewModel newInstance(DteRepository repository, TenantManager tenantManager) {
    return new DteViewModel(repository, tenantManager);
  }
}
