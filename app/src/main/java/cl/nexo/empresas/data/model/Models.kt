package cl.nexo.empresas.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateEmpresaRequest(
    val nombre: String,
    val rut: String,
    val giro: String? = null,
    @SerialName("created_by") val createdBy: String
)
@Serializable
data class Empresa(
    val id: String = "",
    val nombre: String = "",
    val rut: String = "",
    val giro: String? = null,
    val direccion: String? = null,
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("invite_code") val inviteCode: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("created_by") val createdBy: String? = null
)

@Serializable
data class EmpresaMember(
    val id: String = "",
    @SerialName("empresa_id") val empresaId: String = "",
    @SerialName("user_id") val userId: String = "",
    val rol: String = "viewer",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Contacto(
    val id: String = "",
    @SerialName("empresa_id") val empresaId: String = "",
    val nombre: String = "",
    val rut: String? = null,
    val tipo: String = "ambos",
    val activo: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class CuentaCorriente(
    val id: String = "",
    @SerialName("empresa_id") val empresaId: String = "",
    val nombre: String = "",
    val tipo: String = "banco",
    @SerialName("numero_cuenta") val numeroCuenta: String? = null,
    @SerialName("saldo_inicial") val saldoInicial: Long = 0L,
    val activa: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Documento(
    val id: String = "",
    @SerialName("empresa_id") val empresaId: String = "",
    val tipo: String = "egreso",
    @SerialName("numero_documento") val numeroDocumento: String? = null,
    @SerialName("contacto_id") val contactoId: String? = null,
    val descripcion: String = "",
    val categoria: String? = null,
    val monto: Long = 0L,
    @SerialName("cuenta_corriente_id") val cuentaCorrienteId: String? = null,
    @SerialName("fecha_movimiento") val fechaMovimiento: String = "",
    @SerialName("fecha_vencimiento") val fechaVencimiento: String = "",
    val estado: String = "pendiente",
    @SerialName("numero_seguimiento") val numeroSeguimiento: String? = null,
    @SerialName("metodo_pago") val metodoPago: String? = null,
    @SerialName("fecha_pago") val fechaPago: String? = null,
    val notas: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("created_by") val createdBy: String? = null
)

@Serializable
data class Cheque(
    val id: String = "",
    @SerialName("documento_id") val documentoId: String = "",
    @SerialName("empresa_id") val empresaId: String = "",
    @SerialName("numero_cheque") val numeroCheque: String = "",
    val banco: String? = null,
    val monto: Long = 0L,
    @SerialName("fecha_cobro") val fechaCobro: String = "",
    val estado: String = "pendiente",
    val orden: Int = 1,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class AlertaConfig(
    val id: String = "",
    @SerialName("empresa_id") val empresaId: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("dias_anticipacion") val diasAnticipacion: Int = 5,
    @SerialName("alertas_cobros") val alertasCobros: Boolean = true,
    @SerialName("alertas_pagos") val alertasPagos: Boolean = true,
    @SerialName("alertas_cheques") val alertasCheques: Boolean = true,
    @SerialName("push_token") val pushToken: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

enum class TipoDocumento(val value: String, val label: String) {
    INGRESO("ingreso", "Ingreso"),
    EGRESO("egreso", "Egreso")
}

enum class EstadoDocumento(val value: String, val label: String) {
    PENDIENTE("pendiente", "Pendiente"),
    PAGADO("pagado", "Pagado"),
    ANULADO("anulado", "Anulado")
}

enum class MetodoPago(val value: String, val label: String) {
    TRANSFERENCIA("transferencia", "Transferencia"),
    CHEQUE("cheque", "Cheque"),
    EFECTIVO("efectivo", "Efectivo"),
    OTRO("otro", "Otro")
}

enum class CategoriaDocumento(val value: String, val label: String) {
    HONORARIOS("honorarios", "Honorarios"),
    SERVICIOS("servicios", "Servicios"),
    MATERIALES("materiales", "Materiales"),
    ARRIENDO("arriendo", "Arriendo"),
    OTRO("otro", "Otro")
}

enum class RolEmpresa(val value: String) {
    OWNER("owner"),
    VIEWER("viewer")
}
