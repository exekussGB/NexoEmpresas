package cl.nexo.empresas.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DocumentoCreate(
    @SerialName("empresa_id") val empresaId: String,
    val tipo: String,
    @SerialName("numero_documento") val numeroDocumento: String? = null,
    @SerialName("contacto_id") val contactoId: String? = null,
    val descripcion: String,
    val categoria: String? = null,
    val monto: Long,
    @SerialName("cuenta_corriente_id") val cuentaCorrienteId: String? = null,
    @SerialName("fecha_movimiento") val fechaMovimiento: String,
    @SerialName("fecha_vencimiento") val fechaVencimiento: String,
    val estado: String = "pendiente",
    @SerialName("metodo_pago") val metodoPago: String? = null,
    val notas: String? = null,
    @SerialName("created_by") val createdBy: String
)

@Serializable
data class ChequeCreate(
    @SerialName("documento_id") val documentoId: String,
    @SerialName("empresa_id") val empresaId: String,
    @SerialName("numero_cheque") val numeroCheque: String,
    val banco: String? = null,
    val monto: Long,
    @SerialName("fecha_cobro") val fechaCobro: String,
    val orden: Int = 1
)

@Serializable
data class DocumentoMarcarPagado(
    val estado: String = "pagado",
    @SerialName("fecha_pago") val fechaPago: String,
    @SerialName("numero_seguimiento") val numeroSeguimiento: String? = null
)
