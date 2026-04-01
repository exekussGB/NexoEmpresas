package cl.nexo.empresas.data.repository;

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
public final class EmpresasRepositoryImpl_Factory implements Factory<EmpresasRepositoryImpl> {
  private final Provider<SupabaseClient> clientProvider;

  private EmpresasRepositoryImpl_Factory(Provider<SupabaseClient> clientProvider) {
    this.clientProvider = clientProvider;
  }

  @Override
  public EmpresasRepositoryImpl get() {
    return newInstance(clientProvider.get());
  }

  public static EmpresasRepositoryImpl_Factory create(Provider<SupabaseClient> clientProvider) {
    return new EmpresasRepositoryImpl_Factory(clientProvider);
  }

  public static EmpresasRepositoryImpl newInstance(SupabaseClient client) {
    return new EmpresasRepositoryImpl(client);
  }
}
