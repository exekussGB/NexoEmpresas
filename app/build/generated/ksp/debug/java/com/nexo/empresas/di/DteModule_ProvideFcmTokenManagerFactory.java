package com.nexo.empresas.di;

import com.nexo.empresas.core.service.FcmTokenManager;
import com.nexo.empresas.core.session.TenantManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DteModule_ProvideFcmTokenManagerFactory implements Factory<FcmTokenManager> {
  private final Provider<SupabaseClient> supabaseProvider;

  private final Provider<TenantManager> tenantManagerProvider;

  private DteModule_ProvideFcmTokenManagerFactory(Provider<SupabaseClient> supabaseProvider,
      Provider<TenantManager> tenantManagerProvider) {
    this.supabaseProvider = supabaseProvider;
    this.tenantManagerProvider = tenantManagerProvider;
  }

  @Override
  public FcmTokenManager get() {
    return provideFcmTokenManager(supabaseProvider.get(), tenantManagerProvider.get());
  }

  public static DteModule_ProvideFcmTokenManagerFactory create(
      Provider<SupabaseClient> supabaseProvider, Provider<TenantManager> tenantManagerProvider) {
    return new DteModule_ProvideFcmTokenManagerFactory(supabaseProvider, tenantManagerProvider);
  }

  public static FcmTokenManager provideFcmTokenManager(SupabaseClient supabase,
      TenantManager tenantManager) {
    return Preconditions.checkNotNullFromProvides(DteModule.INSTANCE.provideFcmTokenManager(supabase, tenantManager));
  }
}
