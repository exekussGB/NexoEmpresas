package com.nexo.empresas.core.service

import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.nexo.empresas.core.session.TenantManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenManager @Inject constructor(
    private val supabase: SupabaseClient,
    private val tenantManager: TenantManager
) {
    /**
     * Call after successful login and empresa load.
     * Gets the current FCM token and upserts it to Supabase
     * in table fcm_tokens (user_id, empresa_id, token, device_info).
     */
    suspend fun registerCurrentToken() {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        val empresaId = tenantManager.currentEmpresaId ?: return
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            val registration = FcmTokenRegistration(
                userId = userId,
                empresaId = empresaId,
                token = token,
                deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})"
            )
            supabase.from("fcm_tokens").upsert(registration) {
                onConflict = "user_id,empresa_id"
            }
            Log.d("FcmTokenManager", "FCM token registrado exitosamente")
        } catch (e: Exception) {
            Log.w("FcmTokenManager", "No se pudo registrar FCM token: ${e.message}")
        }
    }

    /**
     * Call on logout to invalidate the token in Supabase.
     */
    suspend fun unregisterToken() {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        val empresaId = tenantManager.currentEmpresaId ?: return
        try {
            supabase.from("fcm_tokens").delete {
                filter {
                    eq("user_id", userId)
                    eq("empresa_id", empresaId)
                }
            }
            Log.d("FcmTokenManager", "FCM token eliminado exitosamente")
        } catch (e: Exception) {
            Log.w("FcmTokenManager", "No se pudo eliminar FCM token: ${e.message}")
        }
    }
}

@Serializable
data class FcmTokenRegistration(
    @SerialName("user_id") val userId: String,
    @SerialName("empresa_id") val empresaId: String,
    val token: String,
    @SerialName("device_info") val deviceInfo: String
)
