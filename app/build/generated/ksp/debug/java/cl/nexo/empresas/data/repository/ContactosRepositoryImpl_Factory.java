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
public final class ContactosRepositoryImpl_Factory implements Factory<ContactosRepositoryImpl> {
  private final Provider<SupabaseClient> clientProvider;

  private ContactosRepositoryImpl_Factory(Provider<SupabaseClient> clientProvider) {
    this.clientProvider = clientProvider;
  }

  @Override
  public ContactosRepositoryImpl get() {
    return newInstance(clientProvider.get());
  }

  public static ContactosRepositoryImpl_Factory create(Provider<SupabaseClient> clientProvider) {
    return new ContactosRepositoryImpl_Factory(clientProvider);
  }

  public static ContactosRepositoryImpl newInstance(SupabaseClient client) {
    return new ContactosRepositoryImpl(client);
  }
}
