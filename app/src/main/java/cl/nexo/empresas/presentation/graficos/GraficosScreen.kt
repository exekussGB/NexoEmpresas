package cl.nexo.empresas.presentation.graficos

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import cl.nexo.empresas.data.model.GraficoData
import cl.nexo.empresas.data.model.GraficoMensual
import cl.nexo.empresas.data.model.SaldoCuentaMensual
import cl.nexo.empresas.core.tutorial.TutorialModule
import cl.nexo.empresas.presentation.tutorial.ModuleTutorialLauncher

private val MESES_NOMBRE = listOf("Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic")

private fun Long.formatCorto(): String = when {
    this >= 1_000_000L -> "${this / 1_000_000}M"
    this >= 1_000L     -> "${this / 1_000}K"
    else               -> toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraficosScreen(
    onBack: () -> Unit,
    viewModel: GraficosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val meses   by viewModel.meses.collectAsState()
    val context = LocalContext.current

    // ── SAF: crear documento CSV ────────────────────────────────────────
    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                val csvContent = viewModel.generateCsvContent()
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(csvContent.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "CSV exportado correctamente", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al exportar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gráficos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Botón exportar CSV — solo visible cuando hay datos
                    if (uiState is GraficosUiState.Success) {
                        IconButton(onClick = {
                            val fileName = "graficos_${meses}M_${System.currentTimeMillis()}.csv"
                            csvLauncher.launch(fileName)
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "Exportar CSV")
                        }
                    }
                }
            )
        }
    ) { padding ->
        ModuleTutorialLauncher(TutorialModule.GRAFICOS)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Selector de período ─────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(3, 6, 12).forEach { m ->
                    FilterChip(
                        selected  = meses == m,
                        onClick   = { viewModel.setMeses(m) },
                        label     = { Text("${m}M") }
                    )
                }

                Spacer(Modifier.weight(1f))

                // Botón exportar CSV (texto) — alternativa más visible
                if (uiState is GraficosUiState.Success) {
                    OutlinedButton(
                        onClick = {
                            val fileName = "graficos_${meses}M_${System.currentTimeMillis()}.csv"
                            csvLauncher.launch(fileName)
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("CSV", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // ── Contenido ───────────────────────────────────────────────────
            when (val state = uiState) {
                is GraficosUiState.Loading -> {
                    Box(
                        modifier          = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment  = Alignment.Center
                    ) { CircularProgressIndicator() }
                }
                is GraficosUiState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text     = state.message,
                            modifier = Modifier.padding(16.dp),
                            color    = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                is GraficosUiState.Success -> GraficosContent(data = state.data)
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun GraficosContent(data: GraficoData) {

    // ── Gráfico 1: BARRAS — Cobrado vs Pagado ────────────────────────────
    ChartCard(title = "📊 Cobrado vs Pagado") {
        if (data.mensual.isEmpty()) {
            EmptyMessage()
        } else {
            GroupedBarChart(data.mensual)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot(Color(0xFF4CAF50), "Cobrado (ingresos)")
                LegendDot(Color(0xFFF44336), "Pagado (egresos)")
            }
        }
    }

    // ── Gráfico 2: Barras — Saldo por cuenta ────────────────────────────
    val cuentas = data.porCuenta.map { it.cuentaNombre }.distinct()
    if (cuentas.isNotEmpty()) {
        cuentas.forEach { nombre ->
            ChartCard(title = "🏦 $nombre — Saldo neto mensual") {
                BarChartSaldo(data.porCuenta.filter { it.cuentaNombre == nombre })
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LegendDot(Color(0xFF2196F3), "Ingreso neto")
                    LegendDot(Color(0xFFFF9800), "Egreso neto")
                }
            }
        }
    }
}

// ── Card contenedor ──────────────────────────────────────────────────────────

@Composable
private fun ChartCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier                = Modifier.padding(16.dp),
            verticalArrangement     = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun EmptyMessage() {
    Box(
        modifier         = Modifier.fillMaxWidth().height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Sin datos en el período seleccionado",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// ── Gráfico de BARRAS AGRUPADAS: Cobrado vs Pagado ──────────────────────────

@Composable
private fun GroupedBarChart(data: List<GraficoMensual>) {
    val colorCobrado = Color(0xFF4CAF50) // verde
    val colorPagado  = Color(0xFFF44336) // rojo
    val gridColor    = MaterialTheme.colorScheme.outlineVariant
    val textColor    = MaterialTheme.colorScheme.onSurface
    val density      = LocalDensity.current

    val maxVal = data.maxOf { maxOf(it.totalCobrado, it.totalPagado) }.coerceAtLeast(1L)

    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        val w         = size.width
        val chartH    = size.height - 32.dp.toPx() // espacio para etiquetas abajo
        val n         = data.size
        val groupW    = w / n                        // ancho de cada grupo de barras
        val barW      = (groupW * 0.35f)             // ancho de cada barra individual
        val gap       = groupW * 0.05f               // espacio entre las 2 barras

        // Líneas de grilla horizontales
        for (i in 0..3) {
            val y = chartH * (1f - i / 3f)
            drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 0.5.dp.toPx())
        }

        data.forEachIndexed { i, item ->
            val groupX = groupW * i

            // Barra Cobrado (izquierda)
            val cobradoH = (item.totalCobrado.toFloat() / maxVal * chartH).coerceAtLeast(0f)
            val cobradoX = groupX + (groupW - 2 * barW - gap) / 2f
            drawRoundRect(
                color        = colorCobrado,
                topLeft      = Offset(cobradoX, chartH - cobradoH),
                size         = Size(barW, cobradoH),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )

            // Barra Pagado (derecha)
            val pagadoH = (item.totalPagado.toFloat() / maxVal * chartH).coerceAtLeast(0f)
            val pagadoX = cobradoX + barW + gap
            drawRoundRect(
                color        = colorPagado,
                topLeft      = Offset(pagadoX, chartH - pagadoH),
                size         = Size(barW, pagadoH),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )

            // Valor encima de cada barra (solo si hay espacio)
            if (cobradoH > 8.dp.toPx()) {
                drawContext.canvas.nativeCanvas.drawText(
                    item.totalCobrado.formatCorto(),
                    cobradoX + barW / 2f,
                    chartH - cobradoH - 4.dp.toPx(),
                    android.graphics.Paint().apply {
                        color     = colorCobrado.toArgb()
                        textSize  = with(density) { 9.sp.toPx() }
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                )
            }
            if (pagadoH > 8.dp.toPx()) {
                drawContext.canvas.nativeCanvas.drawText(
                    item.totalPagado.formatCorto(),
                    pagadoX + barW / 2f,
                    chartH - pagadoH - 4.dp.toPx(),
                    android.graphics.Paint().apply {
                        color     = colorPagado.toArgb()
                        textSize  = with(density) { 9.sp.toPx() }
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                )
            }

            // Etiqueta mes (centrada bajo el grupo)
            val monthIdx = item.mes.substring(5, 7).toInt() - 1
            val label    = MESES_NOMBRE.getOrElse(monthIdx) { item.mes.substring(5, 7) }
            drawContext.canvas.nativeCanvas.drawText(
                label,
                groupX + groupW / 2f,
                chartH + 20.dp.toPx(),
                android.graphics.Paint().apply {
                    color    = textColor.toArgb()
                    textSize = with(density) { 10.sp.toPx() }
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

// ── Gráfico de barras: saldo neto por cuenta ────────────────────────────────

@Composable
private fun BarChartSaldo(data: List<SaldoCuentaMensual>) {
    val colorPos  = Color(0xFF2196F3)
    val colorNeg  = Color(0xFFFF9800)
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val density   = LocalDensity.current

    val maxAbs = data.maxOf { kotlin.math.abs(it.saldoNeto) }.coerceAtLeast(1L)

    Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
        val w         = size.width
        val chartH    = size.height - 28.dp.toPx()
        val mid       = chartH / 2f
        val barWidth  = (w / data.size) * 0.55f

        // Línea central
        drawLine(gridColor, Offset(0f, mid), Offset(w, mid), strokeWidth = 1.dp.toPx())

        data.forEachIndexed { i, item ->
            val barX = (w / data.size) * i + (w / data.size - barWidth) / 2f
            val barH = (kotlin.math.abs(item.saldoNeto).toFloat() / maxAbs * (mid - 4.dp.toPx())).coerceAtLeast(0f)
            val top  = if (item.saldoNeto >= 0) mid - barH else mid
            val col  = if (item.saldoNeto >= 0) colorPos else colorNeg

            drawRoundRect(
                color       = col,
                topLeft     = Offset(barX, top),
                size        = Size(barWidth, barH),
                cornerRadius = CornerRadius(3.dp.toPx())
            )

            // Etiqueta mes
            val monthIdx = item.mes.substring(5, 7).toInt() - 1
            val label    = MESES_NOMBRE.getOrElse(monthIdx) { item.mes.substring(5, 7) }
            drawContext.canvas.nativeCanvas.drawText(
                label, barX + barWidth / 2f, chartH + 20.dp.toPx(),
                android.graphics.Paint().apply {
                    color     = textColor.toArgb()
                    textSize  = with(density) { 10.sp.toPx() }
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )

            // Etiqueta valor encima/debajo de la barra
            val valLabel = item.saldoNeto.formatCorto()
            val valY     = if (item.saldoNeto >= 0) top - 4.dp.toPx() else top + barH + 14.dp.toPx()
            drawContext.canvas.nativeCanvas.drawText(
                valLabel, barX + barWidth / 2f, valY,
                android.graphics.Paint().apply {
                    color     = col.toArgb()
                    textSize  = with(density) { 9.sp.toPx() }
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                }
            )
        }
    }
}

// ── Leyenda ──────────────────────────────────────────────────────────────────

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment       = Alignment.CenterVertically,
        horizontalArrangement   = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
