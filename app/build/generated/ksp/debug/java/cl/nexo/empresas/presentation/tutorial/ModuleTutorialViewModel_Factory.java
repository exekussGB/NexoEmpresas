package cl.nexo.empresas.presentation.tutorial;

import cl.nexo.empresas.core.tutorial.TutorialManager;
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
public final class ModuleTutorialViewModel_Factory implements Factory<ModuleTutorialViewModel> {
  private final Provider<TutorialManager> tutorialManagerProvider;

  private ModuleTutorialViewModel_Factory(Provider<TutorialManager> tutorialManagerProvider) {
    this.tutorialManagerProvider = tutorialManagerProvider;
  }

  @Override
  public ModuleTutorialViewModel get() {
    return newInstance(tutorialManagerProvider.get());
  }

  public static ModuleTutorialViewModel_Factory create(
      Provider<TutorialManager> tutorialManagerProvider) {
    return new ModuleTutorialViewModel_Factory(tutorialManagerProvider);
  }

  public static ModuleTutorialViewModel newInstance(TutorialManager tutorialManager) {
    return new ModuleTutorialViewModel(tutorialManager);
  }
}
