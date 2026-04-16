package com.nexo.empresas.dte.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── Tipos de DTE ────────────────────────────────────────────────────────────

enum class TipoDte(val codigo: Int, val descripcion: String) {
    FACTURA_ELECTRONICA(33, "Factura Electrónica"),
    FACTURA_NO_AFECTA(34, "Factura No Afecta"),
    GUIA_DESPACHO(52, "Guía de Despacho"),
    NOTA_DEBITO(56, "Nota de Débito"),
    NOTA_CREDITO(61, "Nota de Crédito"),
    BOLETA_ELECTRONICA(39, "Boleta Electrónica");

    companion object {
        fun fromCodigo(codigo: Int) = entries.firstOrNull { it.codigo == codigo }
    }
}

enum class EstadoDte(val label: String) {
    PENDIENTE("Pendiente"),
    ENVIADO("Enviado al SII"),
    ACEPTADO("Aceptado"),
    ACEPTADO_REPAROS("Aceptado con Reparos"),
    RECHAZADO("Rechazado")
}

// ─── Item de DTE ─────────────────────────────────────────────────────────────

@Serializable
data class ItemDte(
    val id: String? = null,
    @SerialName("dte_id") val dteId: String? = null,
    val descripcion: String,
    val cantidad: Double,
    @SerialName("precio_unitario") val precioUnitario: Long,
    val descuento: Double = 0.0,
    val monto: Long = 0L
) {
    val montoNeto: Long
        get() = ((cantidad * precioUnitario) * (1 - descuento / 100)).toLong()
}

// ─── DTE (Documento Tributario Electrónico) ──────────────────────────────────

@Serializable
data class Dte(
    val id: String? = null,
    @SerialName("empresa_id") val empresaId: String,
    val folio: Int? = null,
    @SerialName("tipo_dte") val tipoDte: Int,
    @SerialName("rut_receptor") val rutReceptor: String,
    @SerialName("razon_social_receptor") val razonSocialReceptor: String,
    @SerialName("giro_receptor") val giroReceptor: String? = null,
    @SerialName("direccion_receptor") val direccionReceptor: String? = null,
    @SerialName("xml_firmado_url") val xmlFirmadoUrl: String? = null,
    @SerialName("pdf_url") val pdfUrl: String? = null,
    @SerialName("estado_sii") val estadoSii: String = EstadoDte.PENDIENTE.name,
    @SerialName("track_id") val trackId: String? = null,
    @SerialName("fecha_emision") val fechaEmision: String? = null,
    @SerialName("monto_total") val montoTotal: Long = 0L,
    @SerialName("monto_neto") val montoNeto: Long = 0L,
    @SerialName("monto_iva") val montoIva: Long = 0L,
    @SerialName("monto_exento") val montoExento: Long = 0L,
    val items: List<ItemDte> = emptyList(),
    @SerialName("created_at") val createdAt: String? = null
) {
    val tipoEnum: TipoDte? get() = TipoDte.fromCodigo(tipoDte)
    val estadoEnum: EstadoDte get() = runCatching { EstadoDte.valueOf(estadoSii) }.getOrDefault(EstadoDte.PENDIENTE)
}

// ─── Folio / CAF ─────────────────────────────────────────────────────────────

@Serializable
data class Folio(
    val id: String? = null,
    @SerialName("empresa_id") val empresaId: String,
    @SerialName("tipo_dte") val tipoDte: Int,
    @SerialName("folio_desde") val folioDesde: Int,
    @SerialName("folio_hasta") val folioHasta: Int,
    @SerialName("folio_actual") val folioActual: Int,
    @SerialName("caf_xml") val cafXml: String? = null
) {
    val disponibles: Int get() = folioHasta - folioActual + 1
    val tipoEnum: TipoDte? get() = TipoDte.fromCodigo(tipoDte)
}

// ─── Requests / Responses ────────────────────────────────────────────────────

@Serializable
data class EmitirDteRequest(
    @SerialName("empresa_id") val empresaId: String,
    @SerialName("tipo_dte") val tipoDte: Int,
    @SerialName("rut_receptor") val rutReceptor: String,
    @SerialName("razon_social_receptor") val razonSocialReceptor: String,
    @SerialName("giro_receptor") val giroReceptor: String? = null,
    @SerialName("direccion_receptor") val direccionReceptor: String? = null,
    val items: List<ItemDteRequest>
)

@Serializable
data class ItemDteRequest(
    val descripcion: String,
    val cantidad: Double,
    @SerialName("precio_unitario") val precioUnitario: Long,
    val descuento: Double = 0.0
)

@Serializable
data class EmitirDteResponse(
    val success: Boolean,
    val dte: Dte? = null,
    val error: String? = null
)

@Serializable
data class EstadoDteResponse(
    @SerialName("track_id") val trackId: String,
    val estado: String,
    val glosa: String? = null
)

@Serializable
data class RutInfo(
    val rut: String,
    @SerialName("razon_social") val razonSocial: String,
    val giro: String? = null,
    val direccion: String? = null
)
