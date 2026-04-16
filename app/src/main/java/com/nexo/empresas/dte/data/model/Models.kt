package com.nexo.empresas.dte.data.model

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

/**
 * Enumeración de tipos de DTE soportados
 */
enum class TipoDte(val codigo: Int, val nombre: String) {
    FACTURA_ELECTRONICA(33, "Factura Electrónica"),
    NOTA_CREDITO(61, "Nota de Crédito"),
    NOTA_DEBITO(56, "Nota de Débito"),
    BOLETA_ELECTRONICA(39, "Boleta Electrónica")
}

// ─── Requests ────────────────────────────────────────────────────────────────

@Serializable
data class EmitirDteRequest(
    val empresaId: String,
    val tipoDte: Int,
    val rutReceptor: String,
    val razonSocialReceptor: String,
    val giroReceptor: String? = null,
    val direccionReceptor: String? = null,
    val items: List<ItemDteRequest> = emptyList()
)

@Serializable
data class ItemDteRequest(
    val descripcion: String,
    val cantidad: Double,
    val precioUnitario: Long,
    val descuento: Double = 0.0
)

// ─── Responses ───────────────────────────────────────────────────────────────

@Serializable
data class EmitirDteResponse(
    val success: Boolean,
    val dte: Dte? = null,
    val error: String? = null
)

@Serializable
data class EstadoDteResponse(
    val estado: String,
    val trackId: String? = null,
    val mensaje: String? = null
)

@Serializable
data class RutInfo(
    val rut: String,
    val razonSocial: String,
    val giro: String? = null,
    val direccion: String? = null
)

// ─── Entidades Principales ──────────────────────────────────────────────────

@Serializable
data class Dte(
    val id: String,
    val empresaId: String,
    val tipoDte: Int,
    val folio: Long,
    val fechaEmision: String,
    val rutEmisor: String,
    val rutReceptor: String,
    val razonSocialReceptor: String,
    val montoNeto: Long,
    val montoIva: Long,
    val montoTotal: Long,
    val estadoEmision: String,
    val estadoSii: String? = null,
    val trackId: String? = null,
    val pdfUrl: String? = null,
    val xmlFirmadoUrl: String? = null,
    val items: List<ItemDte> = emptyList(),
    val creadoEn: String? = null,
    val actualizadoEn: String? = null
)

@Serializable
data class ItemDte(
    val id: String,
    val dteId: String,
    val descripcion: String,
    val cantidad: Double,
    val precioUnitario: Long,
    val descuento: Double = 0.0,
    val montoNeto: Long
)

@Serializable
data class Folio(
    val id: String,
    val empresaId: String,
    val tipoDte: Int,
    val folioActual: Long,
    val folioTope: Long,
    val vigenciaDesde: String,
    val vigenciaHasta: String,
    val estado: String
)
