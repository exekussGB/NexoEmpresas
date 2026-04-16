package com.nexo.empresas.dte

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Servicio FCM para recibir notificaciones del estado de los DTEs.
 *
 * El backend (Supabase Webhook) envía notificaciones push cuando:
 *   - El SII acepta un DTE        → estado = ACEPTADO
 *   - El SII rechaza un DTE       → estado = RECHAZADO
 *   - El SII acepta con reparos   → estado = ACEPTADO_REPAROS
 *
 * Payload esperado en data message:
 *   {
 *     "tipo": "dte_estado",
 *     "dte_id": "<uuid>",
 *     "folio": "123",
 *     "tipo_dte": "Factura Electrónica",
 *     "estado": "ACEPTADO" | "RECHAZADO" | "ACEPTADO_REPAROS",
 *     "glosa": "descripción del estado"
 *   }
 */
class DteFcmService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID_DTE = "dte_estado_channel"
        const val CHANNEL_NAME = "Estado de Documentos Tributarios"
        const val EXTRA_DTE_ID = "dte_id"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        if (data["tipo"] != "dte_estado") return

        val dteId = data["dte_id"] ?: return
        val folio = data["folio"] ?: "-"
        val tipoDte = data["tipo_dte"] ?: "DTE"
        val estado = data["estado"] ?: ""
        val glosa = data["glosa"]

        val (titulo, cuerpo) = when (estado) {
            "ACEPTADO" -> "✅ DTE Aceptado" to "$tipoDte N° $folio fue aceptado por el SII."
            "RECHAZADO" -> "❌ DTE Rechazado" to "$tipoDte N° $folio fue rechazado. ${glosa ?: ""}"
            "ACEPTADO_REPAROS" -> "⚠️ DTE con Reparos" to "$tipoDte N° $folio aceptado con observaciones. ${glosa ?: ""}"
            else -> "📄 Actualización DTE" to "$tipoDte N° $folio: $estado"
        }

        mostrarNotificacion(dteId, titulo, cuerpo)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Enviar el nuevo token a Supabase para asociarlo al usuario/empresa
        // DteTokenManager.updateToken(token)
    }

    private fun mostrarNotificacion(dteId: String, titulo: String, cuerpo: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID_DTE,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de aceptación/rechazo de DTEs por el SII"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(canal)
        }

        // Intent para abrir el detalle del DTE al tocar la notificación
        // Ajusta MainActivity y la ruta según tu setup de navegación
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DTE_ID, dteId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            dteId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(this, CHANNEL_ID_DTE)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Reemplaza con tu ícono
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setStyle(NotificationCompat.BigTextStyle().bigText(cuerpo))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(dteId.hashCode(), notificacion)
    }
}
