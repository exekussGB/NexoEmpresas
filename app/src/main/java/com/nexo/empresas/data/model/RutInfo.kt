package com.nexo.empresas.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RutInfo(
    @SerialName("rut")
    val rut: String,
    
    @SerialName("razon_social")
    val razonSocial: String,
    
    @SerialName("estado")
    val estado: String, // "Activo", "Inactivo"
    
    @SerialName("tipo_contribuyente")
    val tipoContribuyente: String? = null,
    
    @SerialName("giro")
    val giro: String? = null,
    
    @SerialName("fecha_resolucion")
    val fechaResolucion: String? = null
)
