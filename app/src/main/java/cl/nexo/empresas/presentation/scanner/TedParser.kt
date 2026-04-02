package cl.nexo.empresas.presentation.scanner

import cl.nexo.empresas.data.model.DteScanResult
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

/**
 * Parsea el XML del Timbre Electrónico (TED) extraído de un código PDF417,
 * y también URLs de QR del SII chileno.
 * Usa XmlPullParser incluido en el SDK de Android — sin dependencias extra.
 */
object TedParser {

    /**
     * Punto de entrada principal: intenta parsear como TED XML (PDF417) y luego como QR URL (SII).
     */
    fun parse(rawContent: String): DteScanResult? {
        // Try XML TED first (PDF417)
        parseTedXml(rawContent)?.let { return it }
        // Try QR URL (SII)
        parseQrUrl(rawContent)?.let { return it }
        return null
    }

    /**
     * Parsea el XML del TED desde un código PDF417.
     */
    private fun parseTedXml(rawContent: String): DteScanResult? = try {
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
     * Parsea URL de QR del SII chileno.
     * Formato típico: https://...sii.cl/...?RUT=...-K&DV=K&FOLIO=123&FECHA=2024-01-15&MONTO=50000&TIPO=33
     * or: https://palena.sii.cl/cgi_dte/UPL/DTEauth?...
     */
    fun parseQrUrl(rawContent: String): DteScanResult? = try {
        val content = rawContent.trim()
        if (!content.contains("sii.cl", ignoreCase = true) &&
            !content.startsWith("http", ignoreCase = true)) return null

        // Extract parameters from URL
        val params = mutableMapOf<String, String>()
        val queryStart = content.indexOf('?')
        if (queryStart >= 0) {
            content.substring(queryStart + 1).split('&').forEach { param ->
                val parts = param.split('=', limit = 2)
                if (parts.size == 2) {
                    params[parts[0].uppercase()] = java.net.URLDecoder.decode(parts[1], "UTF-8")
                }
            }
        }

        val rut   = params["RUT"] ?: ""
        val folio = params["FOLIO"] ?: ""
        val fecha = params["FECHA"] ?: ""
        val monto = params["MONTO"]?.toLongOrNull() ?: 0L
        val tipo  = params["TIPO"]?.toIntOrNull() ?: 0

        if (folio.isEmpty() || monto == 0L) null
        else DteScanResult(
            rutEmisor     = rut,
            rutReceptor   = "",  // QR usually doesn't have receiver RUT
            tipoDocumento = tipo,
            folio         = folio,
            fechaEmision  = fecha,
            montoTotal    = monto,
            descripcion   = "Escaneado desde QR",
            tipoNexo      = DteScanResult.derivarTipo(tipo)
        )
    } catch (e: Exception) {
        null
    }

    /**
     * Diagnóstico amigable: explica POR QUÉ el código no se pudo parsear.
     * Se usa para mostrar feedback al usuario en la pantalla del scanner.
     */
    fun diagnose(rawContent: String): String {
        val content = rawContent.trim()

        // Check if it's a URL but not SII
        if (content.startsWith("http", ignoreCase = true) &&
            !content.contains("sii.cl", ignoreCase = true)) {
            return "Código QR detectado pero no es del SII.\nSolo se pueden escanear facturas electrónicas chilenas."
        }

        // Check if it's a SII URL but missing data
        if (content.contains("sii.cl", ignoreCase = true)) {
            return "QR del SII detectado pero faltan datos necesarios.\nIntenta escanear el código PDF417 (barra rectangular)."
        }

        // Original TED diagnosis
        // 1. ¿Contiene XML del TED?
        val xmlStart = rawContent.indexOf("<TED")
        if (xmlStart < 0) {
            return "Código detectado pero no contiene un Timbre Electrónico (TED).\nAsegúrate de apuntar al código PDF417 o QR de una factura electrónica del SII."
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
