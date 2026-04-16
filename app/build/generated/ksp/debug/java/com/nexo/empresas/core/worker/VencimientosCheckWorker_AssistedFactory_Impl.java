package com.nexo.empresas.core.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class VencimientosCheckWorker_AssistedFactory_Impl implements VencimientosCheckWorker_AssistedFactory {
  private final VencimientosCheckWorker_Factory delegateFactory;

  VencimientosCheckWorker_AssistedFactory_Impl(VencimientosCheckWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public VencimientosCheckWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<VencimientosCheckWorker_AssistedFactory> create(
      VencimientosCheckWorker_Factory delegateFactory) {
    return InstanceFactory.create(new VencimientosCheckWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<VencimientosCheckWorker_AssistedFactory> createFactoryProvider(
      VencimientosCheckWorker_Factory delegateFactory) {
    return InstanceFactory.create(new VencimientosCheckWorker_AssistedFactory_Impl(delegateFactory));
  }
}
