package com.nexo.empresas.data.repository;

import com.nexo.empresas.core.session.TenantManager;
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
public final class AuthRepositoryImpl_Factory implements Factory<AuthRepositoryImpl> {
  private final Provider<SupabaseClient> clientProvider;

  private final Provider<TenantManager> tenantManagerProvider;

  private AuthRepositoryImpl_Factory(Provider<SupabaseClient> clientProvider,
      Provider<TenantManager> tenantManagerProvider) {
    this.clientProvider = clientProvider;
    this.tenantManagerProvider = tenantManagerProvider;
  }

  @Override
  public AuthRepositoryImpl get() {
    return newInstance(clientProvider.get(), tenantManagerProvider.get());
  }

  public static AuthRepositoryImpl_Factory create(Provider<SupabaseClient> clientProvider,
      Provider<TenantManager> tenantManagerProvider) {
    return new AuthRepositoryImpl_Factory(clientProvider, tenantManagerProvider);
  }

  public static AuthRepositoryImpl newInstance(SupabaseClient client, TenantManager tenantManager) {
    return new AuthRepositoryImpl(client, tenantManager);
  }
}
