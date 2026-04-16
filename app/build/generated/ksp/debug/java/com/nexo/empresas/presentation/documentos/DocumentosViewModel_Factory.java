package com.nexo.empresas.presentation.documentos;

import com.nexo.empresas.core.session.SessionManager;
import com.nexo.empresas.domain.repository.DocumentosRepository;
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
public final class DocumentosViewModel_Factory implements Factory<DocumentosViewModel> {
  private final Provider<DocumentosRepository> repositoryProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  private DocumentosViewModel_Factory(Provider<DocumentosRepository> repositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public DocumentosViewModel get() {
    return newInstance(repositoryProvider.get(), sessionManagerProvider.get());
  }

  public static DocumentosViewModel_Factory create(
      Provider<DocumentosRepository> repositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new DocumentosViewModel_Factory(repositoryProvider, sessionManagerProvider);
  }

  public static DocumentosViewModel newInstance(DocumentosRepository repository,
      SessionManager sessionManager) {
    return new DocumentosViewModel(repository, sessionManager);
  }
}
