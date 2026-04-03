package cl.nexo.empresas.presentation.scanner

import android.util.Log
import cl.nexo.empresas.data.model.DteScanResult
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

/**
 * Parsea el XML del Timbre Electrónico (TED) extraído de un código PDF417,
 * y también URLs de QR del SII chileno.
 * Usa XmlPullParser incluido en el SDK de Android — sin dependencias extra.
 *
 * Los PDF417 de DTE chilenos contienen XML del TED mezclado con datos binarios
 * (firma digital CAF). Este parser maneja múltiples encodings y limpia
 * el contenido antes de buscar la etiqueta <TED>.
 */
object TedParser {

    private const val TAG = "TedParser"

    /**
     * Punto de entrada principal: intenta parsear como TED XML (PDF417) y luego como QR URL (SII).
     * Acepta tanto String (rawValue) como ByteArray (rawBytes) para manejar
     * contenido binario de PDF417.
     */
    fun parse(rawContent: String): DteScanResult? {
        // Try XML TED first (PDF417)
        parseTedXml(rawContent)?.let { return it }
        // Try QR URL (SII)
        parseQrUrl(rawContent)?.let { return it }
        return null
    }

    /**
     * Parsea desde rawBytes directamente — intenta múltiples encodings.
     * Este es el método preferido para PDF417 que contienen datos binarios.
     */
    fun parseFromBytes(rawBytes: ByteArray?): DteScanResult? {
        if (rawBytes == null || rawBytes.isEmpty()) return null

        // Los timbres DTE usan ISO-8859-1 (Latin-1) ya que el XML puede contener
        // caracteres especiales y la firma digital es binaria
        val encodings = listOf("ISO-8859-1", "UTF-8", "US-ASCII", "windows-1252")
        for (encoding in encodings) {
            try {
                val content = String(rawBytes, charset(encoding))
                Log.d(TAG, "Trying encoding $encoding, contains <TED: ${content.contains("<TED")}")
                parseTedXml(content)?.let {
                    Log.d(TAG, "Successfully parsed with encoding: $encoding")
                    return it
                }
            } catch (e: Exception) {
                Log.d(TAG, "Encoding $encoding failed: ${e.message}")
            }
        }

        // Último intento: extraer solo caracteres imprimibles y buscar XML
        try {
            val cleaned = rawBytes.map { byte ->
                val c = byte.toInt() and 0xFF
                if (c in 32..126 || c == 10 || c == 13 || c == 9) c.toChar() else ' '
            }.joinToString("")
            Log.d(TAG, "Cleaned content (first 200): ${cleaned.take(200)}")
            parseTedXml(cleaned)?.let {
                Log.d(TAG, "Parsed from cleaned bytes")
                return it
            }
        } catch (e: Exception) {
            Log.e(TAG, "Cleaned bytes parse failed", e)
        }

        return null
    }

    /**
     * Parsea el XML del TED desde un código PDF417.
     */
    private fun parseTedXml(rawContent: String): DteScanResult? = try {
        // El contenido puede tener bytes binarios antes del XML — buscar <TED
        // Intentar múltiples variantes de cómo puede aparecer la etiqueta
        val xmlStart = findTedStart(rawContent)
        if (xmlStart < 0) {
            Log.d(TAG, "No <TED found. Content preview (first 300 chars): ${rawContent.take(300)}")
            null
        } else {
            var xmlStr = rawContent.substring(xmlStart)

            // Asegurar que el XML termina en </TED> — cortar basura después
            val tedEnd = xmlStr.indexOf("</TED>")
            if (tedEnd > 0) {
                xmlStr = xmlStr.substring(0, tedEnd + "</TED>".length)
            }

            // Limpiar caracteres no-XML que puedan haberse colado (bytes de la firma)
            xmlStr = cleanXmlContent(xmlStr)

            Log.d(TAG, "XML to parse (first 500): ${xmlStr.take(500)}")

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

            Log.d(TAG, "Parsed: folio=$folio, monto=$monto, rut=$rutEmisor, tipo=$tipoDoc")

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
        }
    } catch (e: Exception) {
        Log.e(TAG, "XML parse error", e)
        null // XML malformado o no es un DTE chileno
    }

    /**
     * Busca el inicio de la etiqueta <TED en el contenido.
     * Maneja variantes con/sin espacio, mayúsculas/minúsculas, y caracteres
     * corruptos entre < y TED.
     */
    private fun findTedStart(content: String): Int {
        // Intento directo
        val direct = content.indexOf("<TED")
        if (direct >= 0) return direct

        // Buscar case-insensitive
        val lowerIdx = content.lowercase().indexOf("<ted")
        if (lowerIdx >= 0) return lowerIdx

        // Buscar con posibles caracteres basura entre < y TED
        // Algunos decodificadores insertan bytes nulos o espacios
        val regex = Regex("<\\s*T\\s*E\\s*D", RegexOption.IGNORE_CASE)
        val match = regex.find(content)
        if (match != null) return match.range.first

        return -1
    }

    /**
     * Limpia el contenido XML de caracteres no válidos que pueden venir
     * de la decodificación del PDF417 (bytes de firma digital, etc.)
     */
    private fun cleanXmlContent(xml: String): String {
        val sb = StringBuilder(xml.length)
        var inTag = false
        for (c in xml) {
            when {
                c == '<' -> { inTag = true; sb.append(c) }
                c == '>' -> { inTag = false; sb.append(c) }
                inTag -> {
                    // Dentro de tags: solo permitir caracteres válidos para XML tags
                    if (c.isLetterOrDigit() || c in "/ =\"'_-.:") sb.append(c)
                }
                else -> {
                    // Fuera de tags (contenido): permitir más caracteres
                    val code = c.code
                    if (code == 9 || code == 10 || code == 13 || code in 32..126 ||
                        code in 160..255) {
                        sb.append(c)
                    }
                }
            }
        }
        return sb.toString()
    }

    /**
     * Parsea URL de QR del SII chileno.
     */
    fun parseQrUrl(rawContent: String): DteScanResult? = try {
        val content = rawContent.trim()
        if (!content.contains("sii.cl", ignoreCase = true) &&
            !content.startsWith("http", ignoreCase = true)) return null

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
            rutReceptor   = "",
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

        // 1. ¿Contiene XML del TED?
        val xmlStart = findTedStart(rawContent)
        if (xmlStart < 0) {
            // Log the actual content for debugging
            val preview = rawContent.take(200).map { c ->
                if (c.code in 32..126) c else '?'
            }.joinToString("")
            Log.d(TAG, "diagnose: no TED found. Preview: $preview")
            Log.d(TAG, "diagnose: hex (first 100 bytes): ${rawContent.toByteArray().take(100).joinToString(" ") { String.format("%02X", it) }}")

            return "Código detectado pero no contiene un Timbre Electrónico (TED).\n" +
                   "Asegúrate de apuntar al código PDF417 o QR de una factura electrónica del SII."
        }

        // 2. Intentar parsear y dar razón específica
        return try {
            var xmlStr = rawContent.substring(xmlStart)
            val tedEnd = xmlStr.indexOf("</TED>")
            if (tedEnd > 0) xmlStr = xmlStr.substring(0, tedEnd + "</TED>".length)
            xmlStr = cleanXmlContent(xmlStr)

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
                    "Timbre detectado (Folio: $folio) pero el monto es \$0.\nEste tipo de documento podría no ser compatible."
                else ->
                    "Error desconocido al procesar el timbre. Intenta de nuevo."
            }
        } catch (e: Exception) {
            "Código detectado pero el XML está dañado o incompleto.\nIntenta escanear de nuevo más lento y con buena luz."
        }
    }
}
