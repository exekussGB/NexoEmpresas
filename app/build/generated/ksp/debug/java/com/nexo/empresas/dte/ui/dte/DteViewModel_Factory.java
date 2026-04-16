package com.nexo.empresas.dte.ui.dte;

import com.nexo.empresas.dte.data.repository.DteRepository;
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
public final class DteViewModel_Factory implements Factory<DteViewModel> {
  private final Provider<DteRepository> repositoryProvider;

  private DteViewModel_Factory(Provider<DteRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public DteViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static DteViewModel_Factory create(Provider<DteRepository> repositoryProvider) {
    return new DteViewModel_Factory(repositoryProvider);
  }

  public static DteViewModel newInstance(DteRepository repository) {
    return new DteViewModel(repository);
  }
}
