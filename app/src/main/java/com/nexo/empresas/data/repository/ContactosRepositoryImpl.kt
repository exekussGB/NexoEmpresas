package com.nexo.empresas.data.repository

import com.nexo.empresas.core.util.Constants
import com.nexo.empresas.data.model.Contacto
import com.nexo.empresas.data.model.ContactoUpdate
import com.nexo.empresas.domain.repository.ContactosRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject

/**
 * Implementación del repositorio de Contactos.
 * Gestiona la sincronización de clientes y proveedores desde Supabase Postgrest.
 * Permite realizar búsquedas, filtrado y gestión de la agenda de la empresa.
 */
class ContactosRepositoryImpl @Inject constructor(
    private val client: SupabaseClient
) : ContactosRepository {

    private suspend fun ensureSession() {
        client.auth.awaitInitialization()
    }

    override suspend fun getContactos(empresaId: String): Result<List<Contacto>> = runCatching {
        ensureSession()
        client.from(Constants.TABLE_CONTACTOS)
            .select {
                filter { eq("empresa_id", empresaId) }
            }
            .decodeList<Contacto>()
            .sortedBy { it.nombre }
    }

    override suspend fun createContacto(contacto: Contacto): Result<Contacto> = runCatching {
        ensureSession()
        client.from(Constants.TABLE_CONTACTOS)
            .insert(contacto) { select() }
            .decodeSingle<Contacto>()
    }

    override suspend fun updateContacto(contacto: Contacto): Result<Contacto> = runCatching {
        ensureSession()
        // Usamos ContactoUpdate (sin valores por defecto) para garantizar que
        // todos los campos se serialicen siempre, incluso cuando tipo = "ambos"
        // (que coincide con el default del data class Contacto y kotlinx.serialization
        // lo omitiría si se pasara el objeto completo).
        val payload = ContactoUpdate(
            nombre  = contacto.nombre,
            rut     = contacto.rut,
            tipo    = contacto.tipo,
            activo  = contacto.activo
        )
        client.from(Constants.TABLE_CONTACTOS)
            .update(payload) {
                select()
                filter { eq("id", contacto.id) }
            }
            .decodeSingle<Contacto>()
    }

    override suspend fun toggleActivo(contactoId: String, activo: Boolean): Result<Unit> = runCatching {
        ensureSession()
        client.from(Constants.TABLE_CONTACTOS)
            .update(mapOf("activo" to activo)) {
                filter { eq("id", contactoId) }
            }
    }
}
