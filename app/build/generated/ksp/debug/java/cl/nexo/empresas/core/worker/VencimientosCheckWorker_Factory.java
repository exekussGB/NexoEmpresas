package cl.nexo.empresas.core.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import cl.nexo.empresas.core.session.SessionManager;
import cl.nexo.empresas.domain.repository.AlertasRepository;
import cl.nexo.empresas.domain.repository.ChequesRepository;
import cl.nexo.empresas.domain.repository.DocumentosRepository;
import dagger.internal.DaggerGenerated;
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
public final class VencimientosCheckWorker_Factory {
  private final Provider<AlertasRepository> alertasRepositoryProvider;

  private final Provider<DocumentosRepository> documentosRepositoryProvider;

  private final Provider<ChequesRepository> chequesRepositoryProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  private VencimientosCheckWorker_Factory(Provider<AlertasRepository> alertasRepositoryProvider,
      Provider<DocumentosRepository> documentosRepositoryProvider,
      Provider<ChequesRepository> chequesRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.alertasRepositoryProvider = alertasRepositoryProvider;
    this.documentosRepositoryProvider = documentosRepositoryProvider;
    this.chequesRepositoryProvider = chequesRepositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  public VencimientosCheckWorker get(Context context, WorkerParameters workerParams) {
    return newInstance(context, workerParams, alertasRepositoryProvider.get(), documentosRepositoryProvider.get(), chequesRepositoryProvider.get(), sessionManagerProvider.get());
  }

  public static VencimientosCheckWorker_Factory create(
      Provider<AlertasRepository> alertasRepositoryProvider,
      Provider<DocumentosRepository> documentosRepositoryProvider,
      Provider<ChequesRepository> chequesRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new VencimientosCheckWorker_Factory(alertasRepositoryProvider, documentosRepositoryProvider, chequesRepositoryProvider, sessionManagerProvider);
  }

  public static VencimientosCheckWorker newInstance(Context context, WorkerParameters workerParams,
      AlertasRepository alertasRepository, DocumentosRepository documentosRepository,
      ChequesRepository chequesRepository, SessionManager sessionManager) {
    return new VencimientosCheckWorker(context, workerParams, alertasRepository, documentosRepository, chequesRepository, sessionManager);
  }
}
