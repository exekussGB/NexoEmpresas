package com.nexo.empresas.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemDte(
    @SerialName("numero_linea")
    val numeroLinea: Int,
    
    @SerialName("codigo_interno")
    val codigoInterno: String? = null,
    
    @SerialName("descripcion")
    val descripcion: String,
    
    @SerialName("cantidad")
    val cantidad: Double,
    
    @SerialName("unidad_medida")
    val unidadMedida: String = "Unidades",
    
    @SerialName("precio_unitario")
    val precioUnitario: Long,
    
    @SerialName("descuento")
    val descuento: Long = 0,
    
    @SerialName("monto_item")
    val montoItem: Long
)
