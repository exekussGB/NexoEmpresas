package com.nexo.empresas.presentation.contactos;

import com.nexo.empresas.core.session.TenantManager;
import com.nexo.empresas.domain.repository.ContactosRepository;
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
public final class ContactosViewModel_Factory implements Factory<ContactosViewModel> {
  private final Provider<ContactosRepository> contactosRepositoryProvider;

  private final Provider<TenantManager> tenantManagerProvider;

  private ContactosViewModel_Factory(Provider<ContactosRepository> contactosRepositoryProvider,
      Provider<TenantManager> tenantManagerProvider) {
    this.contactosRepositoryProvider = contactosRepositoryProvider;
    this.tenantManagerProvider = tenantManagerProvider;
  }

  @Override
  public ContactosViewModel get() {
    return newInstance(contactosRepositoryProvider.get(), tenantManagerProvider.get());
  }

  public static ContactosViewModel_Factory create(
      Provider<ContactosRepository> contactosRepositoryProvider,
      Provider<TenantManager> tenantManagerProvider) {
    return new ContactosViewModel_Factory(contactosRepositoryProvider, tenantManagerProvider);
  }

  public static ContactosViewModel newInstance(ContactosRepository contactosRepository,
      TenantManager tenantManager) {
    return new ContactosViewModel(contactosRepository, tenantManager);
  }
}
