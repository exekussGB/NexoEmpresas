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
public final class MultiTrabajadorViewModel_Factory implements Factory<MultiTrabajadorViewModel> {
  @Override
  public MultiTrabajadorViewModel get() {
    return newInstance();
  }

  public static MultiTrabajadorViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MultiTrabajadorViewModel newInstance() {
    return new MultiTrabajadorViewModel();
  }

  private static final class InstanceHolder {
    static final MultiTrabajadorViewModel_Factory INSTANCE = new MultiTrabajadorViewModel_Factory();
  }
}
