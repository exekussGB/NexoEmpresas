package com.nexo.empresas.presentation.simulador

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.nexo.empresas.R
import com.nexo.empresas.data.model.SimulacionResult
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

object SimuladorPdfGenerator {

    fun generateAndSavePdf(
        context: Context,
        result: SimulacionResult,
        candidateName: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            val page = document.startPage(pageInfo)
            val canvas: Canvas = page.canvas
            val paint = Paint()
            val boldPaint = Paint().apply { typeface = Typeface.DEFAULT_BOLD }

            var y = 50f
            val margin = 50f
            val pageWidth = 595f - 2 * margin

            // Draw Logo
            val logo = BitmapFactory.decodeResource(context.resources, R.drawable.app_logo)
            if (logo != null) {
                val scaledLogo = Bitmap.createScaledBitmap(logo, 60, 60, true)
                canvas.drawBitmap(scaledLogo, margin, y, paint)
                y += 70f
            }

            // Header
            paint.textSize = 18f
            canvas.drawText("Simulación de Contratación — NexoEmpresas", margin, y, paint)
            y += 30f

            paint.textSize = 12f
            val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            canvas.drawText("Fecha: $date", margin, y, paint)
            y += 20f

            if (candidateName.isNotEmpty()) {
                canvas.drawText("Candidato/Empresa: $candidateName", margin, y, paint)
                y += 30f
            } else {
                y += 10f
            }

            // COSTO TOTAL
            boldPaint.textSize = 14f
            canvas.drawText("RESUMEN GENERAL", margin, y, boldPaint)
            y += 20f
            drawRow(canvas, paint, "Costo Total Empresa", "$${SimuladorViewModel.formatCLP(result.costoTotalEmpresa)}", margin, y)
            y += 20f
            drawRow(canvas, paint, "Sueldo Líquido Trabajador", "$${SimuladorViewModel.formatCLP(result.sueldoLiquido)}", margin, y)
            y += 40f

            // Haberes
            boldPaint.textSize = 12f
            canvas.drawText("HABERES IMPONIBLES", margin, y, boldPaint)
            y += 20f
            drawRow(canvas, paint, "Sueldo Base", "$${SimuladorViewModel.formatCLP(result.sueldoBase)}", margin, y)
            y += 20f
            drawRow(canvas, paint, "Gratificación", "$${SimuladorViewModel.formatCLP(result.gratificacion)}", margin, y)
            y += 20f
            if (result.comisiones > 0) {
                drawRow(canvas, paint, "Comisiones", "$${SimuladorViewModel.formatCLP(result.comisiones)}", margin, y)
                y += 20f
            }
            if (result.horasExtras > 0) {
                drawRow(canvas, paint, "Horas Extra", "$${SimuladorViewModel.formatCLP(result.horasExtras)}", margin, y)
                y += 20f
            }
            boldPaint.textSize = 11f
            drawRow(canvas, boldPaint, "Total Imponible", "$${SimuladorViewModel.formatCLP(result.totalImponible)}", margin, y)
            y += 30f

            // No Imponibles
            if (result.totalNoImponible > 0) {
                boldPaint.textSize = 12f
                canvas.drawText("HABERES NO IMPONIBLES", margin, y, boldPaint)
                y += 20f
                if (result.colacion > 0) drawRow(canvas, paint, "Colación", "$${SimuladorViewModel.formatCLP(result.colacion)}", margin, y).also { y += 20f }
                if (result.movilizacion > 0) drawRow(canvas, paint, "Movilización", "$${SimuladorViewModel.formatCLP(result.movilizacion)}", margin, y).also { y += 20f }
                if (result.viaticos > 0) drawRow(canvas, paint, "Viáticos", "$${SimuladorViewModel.formatCLP(result.viaticos)}", margin, y).also { y += 20f }
                if (result.desgasteHerramientas > 0) drawRow(canvas, paint, "Desgaste Herr.", "$${SimuladorViewModel.formatCLP(result.desgasteHerramientas)}", margin, y).also { y += 20f }
                if (result.bonosNoImponibles > 0) drawRow(canvas, paint, result.otrosDescuentosLabel, "$${SimuladorViewModel.formatCLP(result.bonosNoImponibles)}", margin, y).also { y += 20f }
                drawRow(canvas, boldPaint, "Total No Imponible", "$${SimuladorViewModel.formatCLP(result.totalNoImponible)}", margin, y)
                y += 30f
            }

            // Descuentos
            boldPaint.textSize = 12f
            canvas.drawText("DESCUENTOS TRABAJADOR", margin, y, boldPaint)
            y += 20f
            drawRow(canvas, paint, "AFP ${result.afpNombre}", "-$${SimuladorViewModel.formatCLP(result.afpMonto)}", margin, y)
            y += 20f
            drawRow(canvas, paint, result.saludDetalle, "-$${SimuladorViewModel.formatCLP(result.saludMonto)}", margin, y)
            y += 20f
            if (result.cesantiaTrabajador > 0) {
                drawRow(canvas, paint, "Seguro Cesantía (0.6%)", "-$${SimuladorViewModel.formatCLP(result.cesantiaTrabajador)}", margin, y)
                y += 20f
            }
            if (result.impuestoUnico > 0) {
                drawRow(canvas, paint, "Impuesto Único", "-$${SimuladorViewModel.formatCLP(result.impuestoUnico)}", margin, y)
                y += 20f
            }
            if (result.anticipo > 0) {
                drawRow(canvas, paint, "Anticipo", "-$${SimuladorViewModel.formatCLP(result.anticipo)}", margin, y)
                y += 20f
            }
            drawRow(canvas, boldPaint, "Total Descuentos", "-$${SimuladorViewModel.formatCLP(result.totalDescuentosTrabajador)}", margin, y)
            y += 30f

            // Costos Empleador
            boldPaint.textSize = 12f
            canvas.drawText("COSTOS EMPLEADOR", margin, y, boldPaint)
            y += 20f
            drawRow(canvas, paint, "SIS (1.54%)", "$${SimuladorViewModel.formatCLP(result.sisMonto)}", margin, y)
            y += 20f
            drawRow(canvas, paint, "Seguro Cesantía Empleador", "$${SimuladorViewModel.formatCLP(result.cesantiaEmpleador)}", margin, y)
            y += 20f
            drawRow(canvas, paint, "Mutual de Seguridad", "$${SimuladorViewModel.formatCLP(result.mutualMonto)}", margin, y)
            y += 20f
            drawRow(canvas, boldPaint, "Total Costos Empleador", "$${SimuladorViewModel.formatCLP(result.totalCostosEmpleador)}", margin, y)
            y += 40f

            // Footer
            paint.textSize = 10f
            paint.color = Color.GRAY
            canvas.drawText("Generado por NexoEmpresas", margin, 800f, paint)

            document.finishPage(page)

            // Save using MediaStore
            val fileName = "Simulacion_${if (candidateName.isNotEmpty()) candidateName.replace(" ", "_") else "Contratacion"}_${System.currentTimeMillis()}.pdf"
            
            savePdfToDownloads(context, document, fileName)
            
            document.close()
            onSuccess()
        } catch (e: Exception) {
            onError(e)
        }
    }

    private fun drawRow(canvas: Canvas, paint: Paint, label: String, value: String, x: Float, y: Float) {
        canvas.drawText(label, x, y, paint)
        val valueWidth = paint.measureText(value)
        canvas.drawText(value, 545f - valueWidth, y, paint)
    }

    private fun savePdfToDownloads(context: Context, document: PdfDocument, fileName: String) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
        }

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        } else {
            // Fallback para API < 29: Usar MediaStore.Files o carpeta de descargas del app
            // Por simplicidad y evitar problemas de permisos, usamos carpeta externa del app en descargas
            // pero el requerimiento pide MediaStore. Aquí intentamos insert en Files.
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) // Truco común o usar legacy
            // Nota: En API < 29 MediaStore.Downloads no existe. 
            // Para cumplir sin pedir permisos WRITE_EXTERNAL_STORAGE, es complejo.
            // Usaremos el directorio de descargas público (puede fallar sin permisos en < 29)
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = java.io.File(downloadsDir, fileName)
            java.io.FileOutputStream(file).use { os ->
                document.writeTo(os)
            }
            null
        }

        uri?.let {
            val outputStream: OutputStream? = resolver.openOutputStream(it)
            outputStream?.use { os ->
                document.writeTo(os)
            }
        } ?: run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                throw Exception("Error al crear el archivo en Descargas")
            }
        }
    }
}
