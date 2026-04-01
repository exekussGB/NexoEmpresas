package cl.nexo.empresas.core.di;

import android.content.Context;
import cl.nexo.empresas.core.session.SupabaseSessionManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.SupabaseClient;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class SupabaseModule_ProvideSupabaseClientFactory implements Factory<SupabaseClient> {
  private final Provider<Context> contextProvider;

  private final Provider<SupabaseSessionManager> sessionManagerProvider;

  private SupabaseModule_ProvideSupabaseClientFactory(Provider<Context> contextProvider,
      Provider<SupabaseSessionManager> sessionManagerProvider) {
    this.contextProvider = contextProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public SupabaseClient get() {
    return provideSupabaseClient(contextProvider.get(), sessionManagerProvider.get());
  }

  public static SupabaseModule_ProvideSupabaseClientFactory create(
      Provider<Context> contextProvider, Provider<SupabaseSessionManager> sessionManagerProvider) {
    return new SupabaseModule_ProvideSupabaseClientFactory(contextProvider, sessionManagerProvider);
  }

  public static SupabaseClient provideSupabaseClient(Context context,
      SupabaseSessionManager sessionManager) {
    return Preconditions.checkNotNullFromProvides(SupabaseModule.INSTANCE.provideSupabaseClient(context, sessionManager));
  }
}
