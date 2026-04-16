package com.nexo.empresas.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Dte(
    @SerialName("id")
    val id: String,
    
    @SerialName("tipo_dte")
    val tipoDte: Int,
    
    @SerialName("folio")
    val folio: Int,
    
    @SerialName("fecha_emision")
    val fechaEmision: String,
    
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
    val iva: Long,
    
    @SerialName("monto_total")
    val montoTotal: Long,
    
    @SerialName("estado")
    val estado: String, // "emitido", "pagado", "rechazado"
    
    @SerialName("xml_url")
    val xmlUrl: String? = null,
    
    @SerialName("pdf_url")
    val pdfUrl: String? = null,
    
    @SerialName("timestamp")
    val timestamp: String? = null
)
