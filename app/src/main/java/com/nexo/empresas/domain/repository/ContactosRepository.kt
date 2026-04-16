package com.nexo.empresas.domain.repository

import com.nexo.empresas.data.model.Contacto

interface ContactosRepository {
    suspend fun getContactos(empresaId: String): Result<List<Contacto>>
    suspend fun createContacto(contacto: Contacto): Result<Contacto>
    suspend fun updateContacto(contacto: Contacto): Result<Contacto>
    suspend fun toggleActivo(contactoId: String, activo: Boolean): Result<Unit>
}
