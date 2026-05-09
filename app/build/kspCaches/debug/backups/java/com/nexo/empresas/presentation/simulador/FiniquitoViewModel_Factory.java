package com.nexo.empresas.presentation.simulador;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class FiniquitoViewModel_Factory implements Factory<FiniquitoViewModel> {
  @Override
  public FiniquitoViewModel get() {
    return newInstance();
  }

  public static FiniquitoViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FiniquitoViewModel newInstance() {
    return new FiniquitoViewModel();
  }

  private static final class InstanceHolder {
    static final FiniquitoViewModel_Factory INSTANCE = new FiniquitoViewModel_Factory();
  }
}
