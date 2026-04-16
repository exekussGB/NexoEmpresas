package com.nexo.empresas.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmitirDteResponse(
    @SerialName("exito")
    val exito: Boolean,
    
    @SerialName("mensaje")
    val mensaje: String,
    
    @SerialName("dte_id")
    val dteId: String? = null,
    
    @SerialName("folio")
    val folio: Int? = null,
    
    @SerialName("fecha_emision")
    val fechaEmision: String? = null,
    
    @SerialName("url_descargar")
    val urlDescargar: String? = null,
    
    @SerialName("xml")
    val xml: String? = null,
    
    @SerialName("codigo_error")
    val codigoError: String? = null
)
