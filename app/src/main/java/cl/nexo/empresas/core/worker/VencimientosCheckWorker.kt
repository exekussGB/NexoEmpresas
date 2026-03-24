package cl.nexo.empresas.core.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cl.nexo.empresas.R
import cl.nexo.empresas.core.session.SessionManager
import cl.nexo.empresas.domain.repository.AlertasRepository
import cl.nexo.empresas.domain.repository.DocumentosRepository
import cl.nexo.empresas.domain.repository.ChequesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@HiltWorker
class VencimientosCheckWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val alertasRepository: AlertasRepository,
    private val documentosRepository: DocumentosRepository,
    private val chequesRepository: ChequesRepository,
    private val sessionManager: SessionManager
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "nexo_vencimientos"
        const val CHANNEL_NAME = "Vencimientos"
    }

    override suspend fun doWork(): Result {
        return try {
            createNotificationChannel()

            // 1. Obtener configuración de alertas
            val configResult = alertasRepository.getConfig()
            val config = configResult.getOrNull() ?: return Result.success()

            val diasAnticipacion = config.diasAnticipacion
            val hoy = LocalDate.now()
            val fechaLimite = hoy.plusDays(diasAnticipacion.toLong())
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            val fechaLimiteStr = fechaLimite.format(formatter)

            var notifId = System.currentTimeMillis().toInt()

            // 2. Verificar documentos pendientes (cobros)
            if (config.alertasCobros) {
                documentosRepository.getDocumentosVencimientoProximo(
                    tipo = "cobro",
                    fechaLimite = fechaLimiteStr
                ).getOrNull()?.forEach { doc ->
                    val mensaje = "Cobro pendiente: ${doc.descripcion} vence el ${doc.fechaVencimiento}"
                    dispararNotificacion(notifId++, "Cobro por vencer", mensaje)
                }
            }

            // 3. Verificar documentos pendientes (pagos)
            if (config.alertasPagos) {
                documentosRepository.getDocumentosVencimientoProximo(
                    tipo = "pago",
                    fechaLimite = fechaLimiteStr
                ).getOrNull()?.forEach { doc ->
                    val mensaje = "Pago pendiente: ${doc.descripcion} vence el ${doc.fechaVencimiento}"
                    dispararNotificacion(notifId++, "Pago por vencer", mensaje)
                }
            }

            // 4. Verificar cheques pendientes
            if (config.alertasCheques) {
                chequesRepository.getChequesVencimientoProximo(
                    fechaLimite = fechaLimiteStr
                ).getOrNull()?.forEach { cheque ->
                    val mensaje = "Cheque N° ${cheque.numeroCheque} con cobro el ${cheque.fechaCobro}"
                    dispararNotificacion(notifId++, "Cheque por cobrar", mensaje)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alertas de vencimientos de documentos y cheques"
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun dispararNotificacion(id: Int, titulo: String, mensaje: String) {
        try {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (e: SecurityException) {
            // Permiso POST_NOTIFICATIONS no concedido, ignorar silenciosamente
        }
    }
}
