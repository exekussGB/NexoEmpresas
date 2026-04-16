package com.nexo.empresas.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmitirDteRequest(
    @SerialName("tipo_dte")
    val tipoDte: Int,
    
    @SerialName("folio")
    val folio: Int,
    
    @SerialName("fecha_emision")
    val fechaEmision: String, // YYYY-MM-DD
    
    @SerialName("rut_emisor")
    val rutEmisor: String,
    
    @SerialName("razon_social_emisor")
    val razonSocialEmisor: String,
    
    @SerialName("rut_receptor")
    val rutReceptor: String,
    
    @SerialName("razon_social_receptor")
    val razonSocialReceptor: String? = null,
    
    @SerialName("items")
    val items: List<ItemDte>,
    
    @SerialName("monto_neto")
    val montoNeto: Long,
    
    @SerialName("iva")
    val iva: Long = 0,
    
    @SerialName("monto_total")
    val montoTotal: Long,
    
    @SerialName("referencia")
    val referencia: String? = null,
    
    @SerialName("forma_pago")
    val formaPago: String? = null // "contado", "credito", etc.
)
