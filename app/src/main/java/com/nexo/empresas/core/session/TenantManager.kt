package com.nexo.empresas.core.session

import com.nexo.empresas.data.model.Empresa
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton que mantiene el contexto de la empresa actualmente seleccionada.
 * Se inyecta en cualquier ViewModel que necesite el empresa_id activo.
 *
 * Bug corregido (empresaId):
 *   ANTES: val empresaId: String get() = empresa?.id ?: ""
 *   → El operador ?: devolvía "" cuando empresa era null, pero "" es tratado
 *     como un ID válido por algunos repositorios, ignorando la guardia de seguridad.
 *
 *   AHORA: empresaId devuelve null cuando no hay empresa activa.
 *   → Cualquier código que intente operar sin empresa activa fallará explícitamente
 *     en lugar de enviar un query con empresa_id = "" a la base de datos.
 *
 *   COMPATIBILIDAD: currentEmpresaId (nullable) es el contrato recomendado.
 *   empresaId (non-null) se mantiene para código legacy que ya existía,
 *   pero lanzará IllegalStateException si se usa sin empresa seleccionada.
 */
@Singleton
class TenantManager @Inject constructor() {

    var empresa: Empresa? = null

    /**
     * ID de la empresa activa. Lanza [IllegalStateException] si no hay
     * empresa seleccionada. Úsalo solo donde estés seguro de que el usuario
     * ya pasó por la pantalla de selección de empresa.
     */
    val empresaId: String
        get() = empresa?.id
            ?: throw IllegalStateException(
                "TenantManager: se intentó acceder a empresaId sin empresa activa. " +
                "Usa currentEmpresaId para verificar primero."
            )

    /**
     * ID de la empresa activa, o null si no hay ninguna seleccionada.
     * Contrato preferido — siempre usa este en repositorios y ViewModels.
     */
    val currentEmpresaId: String?
        get() = empresa?.id?.takeIf { it.isNotBlank() }

    /**
     * Nombre de la empresa activa, o cadena vacía si no hay ninguna.
     */
    val empresaNombre: String
        get() = empresa?.nombre ?: ""

    /**
     * RUT de la empresa activa, o cadena vacía si no hay ninguna.
     */
    val empresaRut: String
        get() = empresa?.rut ?: ""

    /**
     * Limpia el contexto de empresa (p.ej. al cerrar sesión o cambiar empresa).
     */
    fun clearEmpresa() {
        empresa = null
    }

    /**
     * Retorna true si hay una empresa activa seleccionada.
     */
    fun hasEmpresaActiva(): Boolean = empresa != null
}
