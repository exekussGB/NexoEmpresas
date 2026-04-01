package cl.nexo.empresas.presentation.documentos;

import cl.nexo.empresas.core.session.SessionManager;
import cl.nexo.empresas.domain.repository.ContactosRepository;
import cl.nexo.empresas.domain.repository.CuentasCorrientesRepository;
import cl.nexo.empresas.domain.repository.DocumentosRepository;
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
public final class AddDocumentoViewModel_Factory implements Factory<AddDocumentoViewModel> {
  private final Provider<DocumentosRepository> docRepositoryProvider;

  private final Provider<ContactosRepository> contactosRepositoryProvider;

  private final Provider<CuentasCorrientesRepository> cuentasRepositoryProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  private AddDocumentoViewModel_Factory(Provider<DocumentosRepository> docRepositoryProvider,
      Provider<ContactosRepository> contactosRepositoryProvider,
      Provider<CuentasCorrientesRepository> cuentasRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.docRepositoryProvider = docRepositoryProvider;
    this.contactosRepositoryProvider = contactosRepositoryProvider;
    this.cuentasRepositoryProvider = cuentasRepositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public AddDocumentoViewModel get() {
    return newInstance(docRepositoryProvider.get(), contactosRepositoryProvider.get(), cuentasRepositoryProvider.get(), sessionManagerProvider.get());
  }

  public static AddDocumentoViewModel_Factory create(
      Provider<DocumentosRepository> docRepositoryProvider,
      Provider<ContactosRepository> contactosRepositoryProvider,
      Provider<CuentasCorrientesRepository> cuentasRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new AddDocumentoViewModel_Factory(docRepositoryProvider, contactosRepositoryProvider, cuentasRepositoryProvider, sessionManagerProvider);
  }

  public static AddDocumentoViewModel newInstance(DocumentosRepository docRepository,
      ContactosRepository contactosRepository, CuentasCorrientesRepository cuentasRepository,
      SessionManager sessionManager) {
    return new AddDocumentoViewModel(docRepository, contactosRepository, cuentasRepository, sessionManager);
  }
}
