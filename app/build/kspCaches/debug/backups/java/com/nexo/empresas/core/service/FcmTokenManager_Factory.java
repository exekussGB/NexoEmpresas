package com.nexo.empresas.core.service;

import com.nexo.empresas.core.session.TenantManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.SupabaseClient;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class FcmTokenManager_Factory implements Factory<FcmTokenManager> {
  private final Provider<SupabaseClient> supabaseProvider;

  private final Provider<TenantManager> tenantManagerProvider;

  private FcmTokenManager_Factory(Provider<SupabaseClient> supabaseProvider,
      Provider<TenantManager> tenantManagerProvider) {
    this.supabaseProvider = supabaseProvider;
    this.tenantManagerProvider = tenantManagerProvider;
  }

  @Override
  public FcmTokenManager get() {
    return newInstance(supabaseProvider.get(), tenantManagerProvider.get());
  }

  public static FcmTokenManager_Factory create(Provider<SupabaseClient> supabaseProvider,
      Provider<TenantManager> tenantManagerProvider) {
    return new FcmTokenManager_Factory(supabaseProvider, tenantManagerProvider);
  }

  public static FcmTokenManager newInstance(SupabaseClient supabase, TenantManager tenantManager) {
    return new FcmTokenManager(supabase, tenantManager);
  }
}
