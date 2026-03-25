package cl.nexo.empresas.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Resultado del escaneo de un Timbre Electrónico (PDF417) de factura DTE chilena.
 * @Parcelize permite pasarlo via SavedStateHandle entre pantallas de navegación.
 */
@Parcelize
data class DteScanResult(
    val rutEmisor: String,       // <RE> — RUT del proveedor que emite la factura
    val rutReceptor: String,     // <RR> — RUT de tu empresa (debe coincidir)
    val tipoDocumento: Int,      // <TD> — código DTE (33=factura afecta, 39=boleta, etc.)
    val folio: String,           // <F>  — número de factura
    val fechaEmision: String,    // <FE> — fecha ISO "YYYY-MM-DD"
    val montoTotal: Long,        // <MNT> — monto CLP
    val descripcion: String,     // <IT1> — descripción ítem principal
    val tipoNexo: String         // "ingreso" o "egreso" derivado de tipoDocumento
) : Parcelable {

    companion object {
        /** Tipos DTE que generan EGRESO (el proveedor te cobra a ti) */
        private val TIPOS_EGRESO = setOf(33, 34, 39, 41, 56, 61)

        fun derivarTipo(tipoDoc: Int): String =
            if (tipoDoc in TIPOS_EGRESO) "egreso" else "ingreso"

        /** Descripción legible del tipo de documento */
        fun nombreDte(tipoDoc: Int): String = when (tipoDoc) {
            33  -> "Factura Electrónica Afecta"
            34  -> "Factura Electrónica Exenta"
            39  -> "Boleta Electrónica"
            41  -> "Boleta No Afecta"
            56  -> "Nota de Débito"
            61  -> "Nota de Crédito"
            110 -> "Factura de Exportación"
            else -> "DTE tipo $tipoDoc"
        }
    }
}