package com.nexo.empresas.presentation.hub;

import com.nexo.empresas.core.tutorial.TutorialManager;
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
public final class HubViewModel_Factory implements Factory<HubViewModel> {
  private final Provider<TutorialManager> tutorialManagerProvider;

  private HubViewModel_Factory(Provider<TutorialManager> tutorialManagerProvider) {
    this.tutorialManagerProvider = tutorialManagerProvider;
  }

  @Override
  public HubViewModel get() {
    return newInstance(tutorialManagerProvider.get());
  }

  public static HubViewModel_Factory create(Provider<TutorialManager> tutorialManagerProvider) {
    return new HubViewModel_Factory(tutorialManagerProvider);
  }

  public static HubViewModel newInstance(TutorialManager tutorialManager) {
    return new HubViewModel(tutorialManager);
  }
}
