package com.nexo.empresas.presentation.simulador;

import com.nexo.empresas.data.network.IndicadoresService;
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
public final class SimuladorViewModel_Factory implements Factory<SimuladorViewModel> {
  private final Provider<IndicadoresService> indicadoresServiceProvider;

  private SimuladorViewModel_Factory(Provider<IndicadoresService> indicadoresServiceProvider) {
    this.indicadoresServiceProvider = indicadoresServiceProvider;
  }

  @Override
  public SimuladorViewModel get() {
    return newInstance(indicadoresServiceProvider.get());
  }

  public static SimuladorViewModel_Factory create(
      Provider<IndicadoresService> indicadoresServiceProvider) {
    return new SimuladorViewModel_Factory(indicadoresServiceProvider);
  }

  public static SimuladorViewModel newInstance(IndicadoresService indicadoresService) {
    return new SimuladorViewModel(indicadoresService);
  }
}
