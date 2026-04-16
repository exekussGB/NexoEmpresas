package com.nexo.empresas.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EstadoDteResponse(
    @SerialName("dte_id")
    val dteId: String,
    
    @SerialName("folio")
    val folio: Int,
    
    @SerialName("tipo_dte")
    val tipoDte: Int,
    
    @SerialName("estado")
    val estado: String, // "emitido", "pagado", "rechazado", etc.
    
    @SerialName("fecha_emision")
    val fechaEmision: String,
    
    @SerialName("rut_emisor")
    val rutEmisor: String,
    
    @SerialName("rut_receptor")
    val rutReceptor: String,
    
    @SerialName("monto_total")
    val montoTotal: Long,
    
    @SerialName("timestamp")
    val timestamp: String? = null
)
