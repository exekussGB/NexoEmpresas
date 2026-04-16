package com.nexo.empresas.presentation.empresas;

import com.nexo.empresas.core.session.SessionManager;
import com.nexo.empresas.core.session.TenantManager;
import com.nexo.empresas.domain.repository.EmpresasRepository;
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
public final class EmpresasViewModel_Factory implements Factory<EmpresasViewModel> {
  private final Provider<EmpresasRepository> empresasRepositoryProvider;

  private final Provider<TenantManager> tenantManagerProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<SupabaseClient> clientProvider;

  private EmpresasViewModel_Factory(Provider<EmpresasRepository> empresasRepositoryProvider,
      Provider<TenantManager> tenantManagerProvider,
      Provider<SessionManager> sessionManagerProvider, Provider<SupabaseClient> clientProvider) {
    this.empresasRepositoryProvider = empresasRepositoryProvider;
    this.tenantManagerProvider = tenantManagerProvider;
    this.sessionManagerProvider = sessionManagerProvider;
    this.clientProvider = clientProvider;
  }

  @Override
  public EmpresasViewModel get() {
    return newInstance(empresasRepositoryProvider.get(), tenantManagerProvider.get(), sessionManagerProvider.get(), clientProvider.get());
  }

  public static EmpresasViewModel_Factory create(
      Provider<EmpresasRepository> empresasRepositoryProvider,
      Provider<TenantManager> tenantManagerProvider,
      Provider<SessionManager> sessionManagerProvider, Provider<SupabaseClient> clientProvider) {
    return new EmpresasViewModel_Factory(empresasRepositoryProvider, tenantManagerProvider, sessionManagerProvider, clientProvider);
  }

  public static EmpresasViewModel newInstance(EmpresasRepository empresasRepository,
      TenantManager tenantManager, SessionManager sessionManager, SupabaseClient client) {
    return new EmpresasViewModel(empresasRepository, tenantManager, sessionManager, client);
  }
}
