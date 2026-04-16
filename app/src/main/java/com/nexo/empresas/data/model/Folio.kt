package com.nexo.empresas.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Folio(
    @SerialName("id")
    val id: String,
    
    @SerialName("tipo_dte")
    val tipoDte: Int,
    
    @SerialName("folio_actual")
    val folioActual: Int,
    
    @SerialName("folio_maximo")
    val folioMaximo: Int,
    
    @SerialName("rut_empresa")
    val rutEmpresa: String,
    
    @SerialName("fecha_resolucion")
    val fechaResolucion: String,
    
    @SerialName("timestamp")
    val timestamp: String? = null
)
