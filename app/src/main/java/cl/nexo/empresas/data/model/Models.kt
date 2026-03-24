package cl.nexo.empresas.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateEmpresaRequest(
    val nombre: String,
    val rut: String,
    val giro: String? = null,
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

/** UPDATE DTO para Contacto */
@Serializable
data class ContactoUpdate(
    val nombre: String,
    val rut: String?,
    val tipo: String,
    val activo: Boolean
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

/** UPDATE DTO para CuentaCorriente */
@Serializable
data class CuentaCorrienteUpdate(
    val nombre: String,
    val tipo: String,
    @SerialName("numero_cuenta") val numeroCuenta: String?,
    @SerialName("saldo_inicial") val saldoInicial: Long,
    val activa: Boolean
)

// ── Documento ────────────────────────────────────────────────────────────────────────

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
    @SerialName("referencia_doc_id") val referenciaDocId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("created_by") val createdBy: String? = null
)

// DocumentoCreate está definido en DocumentoCreate.kt

/** UPDATE DTO para marcar un documento como pagado */
@Serializable
data class DocumentoMarcarPagado(
    val estado: String = "pagado",
    @SerialName("fecha_pago")           val fechaPago: String,
    @SerialName("numero_seguimiento")   val numeroSeguimiento: String? = null
)

// ── Cheque ──────────────────────────────────────────────────────────────────────────

@Serializable
data class Cheque(
    val id: String = "",
    @SerialName("documento_id") val documentoId: String = "",
    @SerialName("empresa_id")   val empresaId: String = "",
    @SerialName("numero_cheque") val numeroCheque: String = "",
    val banco: String? = null,
    val monto: Long = 0L,
    @SerialName("fecha_cobro")  val fechaCobro: String = "",
    val estado: String = "pendiente",
    val orden: Int = 1,
    @SerialName("created_at")   val createdAt: String? = null
)

/** INSERT DTO para Cheque — `orden` sin default para forzar serialización */
@Serializable
data class ChequeCreate(
    @SerialName("documento_id")  val documentoId: String,
    @SerialName("empresa_id")    val empresaId: String,
    @SerialName("numero_cheque") val numeroCheque: String,
    val banco: String? = null,
    val monto: Long,
    @SerialName("fecha_cobro")   val fechaCobro: String,
    val estado: String = "pendiente",
    val orden: Int
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

// ── Módulo 7: Gráficos ─────────────────────────────────────────────────────────────────────

@Serializable
data class GraficoMensual(
    val mes: String,
    @SerialName("total_cobrado") val totalCobrado: Long,
    @SerialName("total_pagado")  val totalPagado:  Long
)

@Serializable
data class SaldoCuentaMensual(
    @SerialName("cuenta_nombre") val cuentaNombre: String,
    val mes:                                        String,
    @SerialName("saldo_neto")    val saldoNeto:     Long
)

@Serializable
data class GraficoData(
    val mensual:                                          List<GraficoMensual>,
    @SerialName("por_cuenta") val porCuenta: List<SaldoCuentaMensual>
)

@Serializable
data class GraficoParams(
    @SerialName("p_empresa_id") val empresaId: String,
    @SerialName("p_meses")      val meses:     Int
)

// ── Módulo 3: Dashboard ────────────────────────────────────────────────────────────────────

@Serializable
data class CuentaDashboard(
    val id: String = "",
    val nombre: String = "",
    val tipo: String = "banco",
    @SerialName("saldo_inicial") val saldoInicial: Long = 0L,
    val ingresos: Long = 0L,
    val egresos: Long = 0L
) {
    val saldo: Long get() = saldoInicial + ingresos - egresos
}

@Serializable
data class DashboardTotales(
    @SerialName("total_por_cobrar") val totalPorCobrar: Long = 0L,
    @SerialName("total_por_pagar") val totalPorPagar: Long = 0L,
    @SerialName("total_cheques_pendientes") val totalChequesPendientes: Long = 0L,
    val cuentas: List<CuentaDashboard> = emptyList()
)
