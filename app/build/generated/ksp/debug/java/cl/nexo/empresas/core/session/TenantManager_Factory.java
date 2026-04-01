package cl.nexo.empresas.core.session;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
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
public final class TenantManager_Factory implements Factory<TenantManager> {
  @Override
  public TenantManager get() {
    return newInstance();
  }

  public static TenantManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TenantManager newInstance() {
    return new TenantManager();
  }

  private static final class InstanceHolder {
    static final TenantManager_Factory INSTANCE = new TenantManager_Factory();
  }
}
