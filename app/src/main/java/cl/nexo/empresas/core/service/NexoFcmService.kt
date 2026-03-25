package cl.nexo.empresas.core.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cl.nexo.empresas.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Servicio FCM que maneja:
 * 1. onNewToken → guarda el token en SharedPreferences para que AlertasViewModel lo suba a Supabase
 * 2. onMessageReceived → muestra la notificación cuando la app está en foreground
 */
class NexoFcmService : FirebaseMessagingService() {

    companion object {
        const val PREFS_NAME  = "nexo_fcm_prefs"
        const val KEY_TOKEN   = "fcm_token_pending"
        const val CHANNEL_ID  = "nexo_vencimientos"
        const val CHANNEL_NAME = "Vencimientos"
    }

    /** Llamado cuando Firebase asigna/rota el token del dispositivo. */
    override fun onNewToken(token: String) {
        // Guardamos en prefs; AlertasViewModel lo leerá y subirá a Supabase
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }

    /** Llamado cuando llega un mensaje y la app está en FOREGROUND. */
    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "NexoEmpresas"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: return          // sin cuerpo → no mostrar nada

        mostrarNotificacion(title, body)
    }

    // ────────────────────────────────────────────────────────────────────────

    private fun mostrarNotificacion(title: String, body: String) {
        crearCanalSiNecesario()

        try {
            val notif = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(this)
                .notify(System.currentTimeMillis().toInt(), notif)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS no concedido — ignorar silenciosamente
        }
    }

    private fun crearCanalSiNecesario() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas de vencimientos de documentos y cheques"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }
    }
}
