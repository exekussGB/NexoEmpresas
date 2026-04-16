package com.nexo.empresas.presentation.tutorial;

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
public final class TutorialListViewModel_Factory implements Factory<TutorialListViewModel> {
  private final Provider<TutorialManager> tutorialManagerProvider;

  private TutorialListViewModel_Factory(Provider<TutorialManager> tutorialManagerProvider) {
    this.tutorialManagerProvider = tutorialManagerProvider;
  }

  @Override
  public TutorialListViewModel get() {
    return newInstance(tutorialManagerProvider.get());
  }

  public static TutorialListViewModel_Factory create(
      Provider<TutorialManager> tutorialManagerProvider) {
    return new TutorialListViewModel_Factory(tutorialManagerProvider);
  }

  public static TutorialListViewModel newInstance(TutorialManager tutorialManager) {
    return new TutorialListViewModel(tutorialManager);
  }
}
