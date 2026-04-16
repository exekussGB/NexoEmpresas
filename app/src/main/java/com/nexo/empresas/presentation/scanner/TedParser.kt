package com.nexo.empresas.presentation.scanner

import com.nexo.empresas.data.model.DteScanResult
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

/**
 * Parsea el XML del Timbre Electrónico (TED) extraído de un código PDF417.
 * Usa XmlPullParser incluido en el SDK de Android — sin dependencias extra.
 */
object TedParser {

    fun parse(rawContent: String): DteScanResult? = try {
        // El contenido puede tener bytes binarios antes del XML — buscar <TED
        val xmlStart = rawContent.indexOf("<TED")
        if (xmlStart < 0) return null
        val xmlStr = rawContent.substring(xmlStart)

        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xmlStr))

        var rutEmisor   = ""
        var rutReceptor = ""
        var tipoDoc     = 0
        var folio       = ""
        var fecha       = ""
        var monto       = 0L
        var descripcion = ""
        var currentTag  = ""

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG ->
                    currentTag = parser.name ?: ""
                XmlPullParser.TEXT -> {
                    val text = parser.text?.trim() ?: ""
                    when (currentTag) {
                        "RE"  -> rutEmisor   = text
                        "RR"  -> rutReceptor = text
                        "TD"  -> tipoDoc     = text.toIntOrNull() ?: 0
                        "F"   -> folio       = text
                        "FE"  -> fecha       = text
                        "MNT" -> monto       = text.toLongOrNull() ?: 0L
                        "IT1" -> if (descripcion.isEmpty()) descripcion = text
                    }
                }
            }
            eventType = parser.next()
        }

        // Validar campos mínimos obligatorios
        if (folio.isEmpty() || monto == 0L) null
        else DteScanResult(
            rutEmisor    = rutEmisor,
            rutReceptor  = rutReceptor,
            tipoDocumento = tipoDoc,
            folio        = folio,
            fechaEmision = fecha,
            montoTotal   = monto,
            descripcion  = descripcion,
            tipoNexo     = DteScanResult.derivarTipo(tipoDoc)
        )
    } catch (e: Exception) {
        null // XML malformado o no es un DTE chileno
    }
}