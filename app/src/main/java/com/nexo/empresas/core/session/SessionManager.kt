package com.nexo.empresas.core.session

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestiona el estado de sesión del usuario: info de auth (userId, email)
 * más contexto de empresa activa (delegado a TenantManager).
 */
@Singleton
class SessionManager @Inject constructor(
    private val supabase: SupabaseClient,
    private val tenantManager: TenantManager
) {
    // ── Empresa context (delegado a TenantManager) ────────────────────────
    val empresaId: String get() = tenantManager.empresaId
    val currentEmpresaId: String? get() = tenantManager.empresaId.takeIf { it.isNotBlank() }

    // ── Info del usuario autenticado ──────────────────────────────────────
    val userId: String get() = supabase.auth.currentUserOrNull()?.id ?: ""
    val currentUserId: String? get() = supabase.auth.currentUserOrNull()?.id
    val userEmail: String get() = supabase.auth.currentUserOrNull()?.email ?: ""

    // ── Rol en la empresa activa (se actualiza al seleccionar empresa) ─────
    var userRole: String = "viewer"
    val currentRol: String get() = userRole

    // ── Cerrar sesión ─────────────────────────────────────────────────────
    fun clearSession() {
        tenantManager.empresa = null
        userRole = "viewer"
        CoroutineScope(Dispatchers.IO).launch {
            runCatching { supabase.auth.signOut() }
        }
    }
}
