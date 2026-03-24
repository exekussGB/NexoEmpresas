package cl.nexo.empresas.core.session

import cl.nexo.empresas.data.model.Empresa
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton que mantiene el contexto de la empresa actualmente seleccionada.
 * Se inyecta en cualquier ViewModel que necesite el empresa_id activo.
 */
@Singleton
class TenantManager @Inject constructor() {
    var empresa: Empresa? = null
    val empresaId: String get() = empresa?.id ?: ""
    val empresaNombre: String get() = empresa?.nombre ?: ""
    val empresaRut: String get() = empresa?.rut ?: ""
}
