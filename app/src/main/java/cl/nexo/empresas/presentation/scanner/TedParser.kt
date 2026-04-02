package cl.nexo.empresas.presentation.scanner

import cl.nexo.empresas.data.model.DteScanResult
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

    /**
     * Diagnóstico amigable: explica POR QUÉ el código no se pudo parsear.
     * Se usa para mostrar feedback al usuario en la pantalla del scanner.
     */
    fun diagnose(rawContent: String): String {
        // 1. ¿Contiene XML del TED?
        val xmlStart = rawContent.indexOf("<TED")
        if (xmlStart < 0) {
            return "Código detectado pero no contiene un Timbre Electrónico (TED).\nAsegúrate de apuntar al código PDF417 de una factura electrónica del SII."
        }

        // 2. Intentar parsear y dar razón específica
        return try {
            val xmlStr = rawContent.substring(xmlStart)
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xmlStr))

            var folio = ""
            var monto = 0L
            var currentTag = ""

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> currentTag = parser.name ?: ""
                    XmlPullParser.TEXT -> {
                        val text = parser.text?.trim() ?: ""
                        when (currentTag) {
                            "F"   -> folio = text
                            "MNT" -> monto = text.toLongOrNull() ?: 0L
                        }
                    }
                }
                eventType = parser.next()
            }

            when {
                folio.isEmpty() && monto == 0L ->
                    "Timbre detectado pero no se encontró folio ni monto.\nEl documento podría estar dañado o ser un formato no soportado."
                folio.isEmpty() ->
                    "Timbre detectado pero falta el número de folio.\nIntenta escanear de nuevo con mejor iluminación."
                monto == 0L ->
                    "Timbre detectado (Folio: $folio) pero el monto es $0.\nEste tipo de documento podría no ser compatible."
                else ->
                    "Error desconocido al procesar el timbre. Intenta de nuevo."
            }
        } catch (e: Exception) {
            "Código detectado pero el XML está dañado o incompleto.\nIntenta escanear de nuevo más lento y con buena luz."
        }
    }
}
