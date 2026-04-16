package com.nexo.empresas.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DocumentoCreate(
    @SerialName("empresa_id")          val empresaId: String,
    @SerialName("tipo")                val tipo: String,
    @SerialName("numero_documento")    val numeroDocumento: String? = null,
    @SerialName("contacto_id")         val contactoId: String? = null,
    @SerialName("descripcion")         val descripcion: String,
    @SerialName("categoria")           val categoria: String? = null,
    @SerialName("monto")               val monto: Long,
    @SerialName("cuenta_corriente_id") val cuentaCorrienteId: String? = null,
    @SerialName("fecha_movimiento")    val fechaMovimiento: String,
    @SerialName("fecha_vencimiento")   val fechaVencimiento: String,
    @SerialName("metodo_pago")         val metodoPago: String? = null,
    @SerialName("notas")               val notas: String? = null,
    @SerialName("referencia_doc_id")   val referenciaDocId: String? = null,
    @SerialName("created_by")          val createdBy: String
)
