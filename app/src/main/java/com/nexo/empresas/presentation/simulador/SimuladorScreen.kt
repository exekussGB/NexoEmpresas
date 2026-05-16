package com.nexo.empresas.presentation.simulador

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nexo.empresas.data.model.*
import com.nexo.empresas.data.model.RemuneracionesChile as RC
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimuladorScreen(
    onBack: () -> Unit,
    onNavigateToFiniquito: () -> Unit,
    onNavigateToMulti: (Long) -> Unit,
    viewModel: SimuladorViewModel = hiltViewModel()
) {
    val input by viewModel.input.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()
    val isFetchingUtm by viewModel.isFetchingUtm.collectAsStateWithLifecycle()
    val comparacion by viewModel.comparacion.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Simulador Contratación") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    if (result != null) {
                        IconButton(onClick = {
                            result?.let { r ->
                                SimuladorPdfGenerator.generateAndSavePdf(
                                    context = context,
                                    result = r,
                                    candidateName = input.nombreCandidato,
                                    onSuccess = {
                                        scope.launch { snackbarHostState.showSnackbar("PDF guardado en Descargas") }
                                    },
                                    onError = { e ->
                                        scope.launch { snackbarHostState.showSnackbar("Error: ${e.message}") }
                                    }
                                )
                            }
                        }) {
                            Icon(Icons.Default.PictureAsPdf, "Exportar PDF")
                        }
                    }
                    IconButton(onClick = { viewModel.reset() }) {
                        Icon(Icons.Default.Refresh, "Reiniciar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ═══════════════════════════════════════════
            // 1. PRETENSION LÍQUIDA
            // ═══════════════════════════════════════════
            SectionCard("Pretensión de Renta") {
                MoneyField(
                    label = "¿Cuánto quiere ganar al bolsillo? *",
                    value = input.sueldoLiquidoDeseado,
                    onValueChange = { v -> viewModel.updateInput { copy(sueldoLiquidoDeseado = v) } }
                )

                OutlinedTextField(
                    value = input.nombreCandidato,
                    onValueChange = { v -> viewModel.updateInput { copy(nombreCandidato = v) } },
                    label = { Text("Nombre del Candidato / Empresa (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Info card for shortfall (optimization structure)
                val isShortfall = result?.totalNoImponible ?: 0L > 0
                if (isShortfall) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Estructura optimizada", style = MaterialTheme.typography.labelLarge)
                                Text(
                                    "Para alcanzar el líquido deseado, se fijó el sueldo base en el mínimo legal ($${SimuladorViewModel.formatCLP(RC.INGRESO_MINIMO)}) y se sugirieron haberes no imponibles para reducir el costo de contratación.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                
                if (input.warningPorDebajoMinimo) {
                    Text(
                        "⚠️ El sueldo mínimo ya supera la pretensión ingresada.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (input.errorSueldoExcedido) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Los haberes manuales exceden el sueldo deseado. Ajusta los valores para cuadrar el líquido.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════
            // 2. CONTRATO Y PREVISIÓN
            // ═══════════════════════════════════════════
            SectionCard("Contrato y Previsión") {
                // AFP
                Text("AFP", style = MaterialTheme.typography.labelMedium)
                var afpExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = afpExpanded, onExpandedChange = { afpExpanded = it }) {
                    OutlinedTextField(
                        value = "${RC.AFP_LIST[input.afpIndex].nombre} (${RC.AFP_LIST[input.afpIndex].total}%)",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(afpExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(afpExpanded, { afpExpanded = false }) {
                        RC.AFP_LIST.forEachIndexed { idx, afp ->
                            DropdownMenuItem(
                                text = { Text("${afp.nombre} — ${afp.total}% (comisión ${afp.comision}%)") },
                                onClick = {
                                    viewModel.updateInput { copy(afpIndex = idx) }
                                    afpExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Salud
                Text("Sistema de Salud", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TipoSalud.entries.forEach { tipo ->
                        FilterChip(
                            selected = input.tipoSalud == tipo,
                            onClick = { viewModel.updateInput { copy(tipoSalud = tipo) } },
                            label = { Text(tipo.label) }
                        )
                    }
                }
                AnimatedVisibility(input.tipoSalud == TipoSalud.ISAPRE) {
                    MoneyField(
                        label = "Cotización Isapre (pesos)",
                        value = input.cotizacionIsapre,
                        onValueChange = { v -> viewModel.updateInput { copy(cotizacionIsapre = v) } }
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Tipo contrato
                Text("Tipo de Contrato", style = MaterialTheme.typography.labelMedium)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    TipoContrato.entries.forEachIndexed { idx, tipo ->
                        SegmentedButton(
                            selected = input.tipoContrato == tipo,
                            onClick = { viewModel.updateInput { copy(tipoContrato = tipo) } },
                            shape = SegmentedButtonDefaults.itemShape(idx, TipoContrato.entries.size)
                        ) { Text(tipo.label, fontSize = 11.sp) }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Gratificación
                Text("Gratificación Legal", style = MaterialTheme.typography.labelMedium)
                var gratExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = gratExpanded, onExpandedChange = { gratExpanded = it }) {
                    OutlinedTextField(
                        value = input.gratificacionTipo.label,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(gratExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(gratExpanded, { gratExpanded = false }) {
                        GratificacionTipo.entries.forEach { tipo ->
                            DropdownMenuItem(
                                text = { Text(tipo.label) },
                                onClick = {
                                    viewModel.updateInput { copy(gratificacionTipo = tipo) }
                                    gratExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════
            // 3. OTROS HABERES
            // ═══════════════════════════════════════════
            SectionCard("Otros Haberes (opcional)") {
                // IMPONIBLES
                Text("Haberes Imponibles", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("Afectan AFP, salud e impuesto", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoneyField("Comisiones", input.comisiones, { v -> viewModel.updateInput { copy(comisiones = v) } }, Modifier.weight(1f))
                    MoneyField("Bonos Imp.", input.bonosImponibles, { v -> viewModel.updateInput { copy(bonosImponibles = v) } }, Modifier.weight(1f))
                }
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = if (input.horasExtraCount > 0) input.horasExtraCount.toString() else "",
                        onValueChange = { text ->
                            val v = text.filter { it.isDigit() }.toIntOrNull() ?: 0
                            viewModel.updateInput { copy(horasExtraCount = v) }
                        },
                        label = { Text("Num. Horas Extra") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    if (input.horasExtraCount > 0) {
                        val valorHora = (RC.INGRESO_MINIMO / 30.0 / 8.0 * 1.5).roundToLong()
                        val totalHE = valorHora * input.horasExtraCount
                        Text(
                            "Valor hora: \$${SimuladorViewModel.formatCLP(valorHora)}  |  Total: \$${SimuladorViewModel.formatCLP(totalHE)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // NO IMPONIBLES
                Text("Haberes No Imponibles", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("No afectan AFP ni salud", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        MoneyField("Colación", input.colacion, { v -> viewModel.updateInput { copy(colacion = v) } })
                        if (!input.colacionManual && input.colacion > 0) InfoBadge("Sugerido")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        MoneyField("Movilización", input.movilizacion, { v -> viewModel.updateInput { copy(movilizacion = v) } })
                        if (!input.movilizacionManual && input.movilizacion > 0) InfoBadge("Sugerido")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        MoneyField("Viáticos", input.viaticos, { v -> viewModel.updateInput { copy(viaticos = v) } })
                        if (!input.viaticosManual && input.viaticos > 0) InfoBadge("Sugerido")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        MoneyField("Desgaste Herr.", input.desgasteHerramientas, { v -> viewModel.updateInput { copy(desgasteHerramientas = v) } })
                        if (!input.desgasteHerramientasManual && input.desgasteHerramientas > 0) InfoBadge("Sugerido")
                    }
                }
                Column {
                    MoneyField("Otros no imponibles", input.bonosNoImponibles, { v -> viewModel.updateInput { copy(bonosNoImponibles = v) } })
                    if (!input.otrosNoImponiblesManual && input.bonosNoImponibles > 0) InfoBadge("Sugerido")
                }
            }

            // ═══════════════════════════════════════════
            // 4. MUTUAL
            // ═══════════════════════════════════════════
            SectionCard("Mutual de Seguridad") {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = "0.93",
                        readOnly = true,
                        onValueChange = {},
                        label = { Text("Tasa base") },
                        suffix = { Text("%") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    Text(" + ", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 8.dp))
                    OutlinedTextField(
                        value = if (input.tasaMutualAdicional > 0) input.tasaMutualAdicional.toString() else "",
                        onValueChange = { text ->
                            val v = text.toDoubleOrNull() ?: 0.0
                            viewModel.updateInput { copy(tasaMutualAdicional = v.coerceIn(0.0, 3.4)) }
                        },
                        label = { Text("Adicional") },
                        suffix = { Text("%") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
                Text("Tasa total: ${String.format("%.2f", 0.93 + input.tasaMutualAdicional)}%", style = MaterialTheme.typography.bodySmall)
            }

            // ═══════════════════════════════════════════
            // 5. OTROS DESCUENTOS
            // ═══════════════════════════════════════════
            SectionCard("Otros Descuentos") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoneyField("Anticipo", input.anticipo, { v -> viewModel.updateInput { copy(anticipo = v) } }, Modifier.weight(1f))
                    MoneyField("Préstamo empresa", input.prestamoEmpresa, { v -> viewModel.updateInput { copy(prestamoEmpresa = v) } }, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = input.otrosDescuentosLabel,
                        onValueChange = { v -> viewModel.updateInput { copy(otrosDescuentosLabel = v) } },
                        label = { Text("Concepto") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    MoneyField("Monto", input.otrosDescuentos, { v -> viewModel.updateInput { copy(otrosDescuentos = v) } }, Modifier.weight(1f))
                }
            }

            // ═══════════════════════════════════════════
            // RESULTADOS (LIQUIDACIÓN STYLE)
            // ═══════════════════════════════════════════
            AnimatedVisibility(result != null) {
                result?.let { r ->
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                        // BLOCK A — INGRESOS
                        LiquidacionBlock("INGRESOS", Color(0xFF4CAF50)) {
                            ResultRow("Sueldo Base (mínimo legal)", r.sueldoBase, prefix = "+ ")
                            ResultRow("Gratificación", r.gratificacion, prefix = "+ ")
                            if (r.horasExtras > 0) ResultRow("Horas Extra", r.horasExtras, prefix = "+ ")
                            if (r.comisiones > 0) ResultRow("Comisiones", r.comisiones, prefix = "+ ")
                            if (r.bonosImponibles > 0) ResultRow("Bonos Imponibles", r.bonosImponibles, prefix = "+ ")
                            HorizontalDivider(Modifier.padding(vertical = 4.dp))
                            ResultRow("Total Imponible", r.totalImponible, bold = true)
                            
                            if (r.totalNoImponible > 0) {
                                Spacer(Modifier.height(8.dp))
                                if (r.colacion > 0) ResultRow("Colación", r.colacion, prefix = "+ ")
                                if (r.movilizacion > 0) ResultRow("Movilización", r.movilizacion, prefix = "+ ")
                                if (r.viaticos > 0) ResultRow("Viáticos", r.viaticos, prefix = "+ ")
                                if (r.desgasteHerramientas > 0) ResultRow("Desgaste Herr.", r.desgasteHerramientas, prefix = "+ ")
                                if (r.bonosNoImponibles > 0) ResultRow("Otros no imponibles", r.bonosNoImponibles, prefix = "+ ")
                                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                                ResultRow("Total Haberes", r.totalHaberes, bold = true)
                            }
                        }

                        // BLOCK B — DESCUENTOS
                        LiquidacionBlock("DESCUENTOS TRABAJADOR", MaterialTheme.colorScheme.error) {
                            ResultRow("AFP ${r.afpNombre}", r.afpMonto, negative = true, prefix = "- ")
                            ResultRow("Salud ${r.saludDetalle}", r.saludMonto, negative = true, prefix = "- ")
                            if (r.cesantiaTrabajador > 0) ResultRow("Seg. Cesantía (0,6%)", r.cesantiaTrabajador, negative = true, prefix = "- ")
                            if (r.impuestoUnico > 0) ResultRow("Impuesto Único (${String.format("%.1f", r.tasaEfectivaImpuesto)}%)", r.impuestoUnico, negative = true, prefix = "- ")
                            if (r.anticipo > 0) ResultRow("Anticipo", r.anticipo, negative = true, prefix = "- ")
                            if (r.prestamoEmpresa > 0) ResultRow("Préstamo empresa", r.prestamoEmpresa, negative = true, prefix = "- ")
                            if (r.otrosDescuentos > 0) ResultRow(r.otrosDescuentosLabel, r.otrosDescuentos, negative = true, prefix = "- ")
                            HorizontalDivider(Modifier.padding(vertical = 4.dp))
                            ResultRow("Total Descuentos", r.totalDescuentosTrabajador, bold = true, negative = true)
                        }

                        // BLOCK C — COSTO EMPLEADOR
                        LiquidacionBlock("COSTO ADICIONAL EMPLEADOR", MaterialTheme.colorScheme.primary) {
                            ResultRow("SIS (1,54%)", r.sisMonto, prefix = "+ ")
                            val cesantiaLabel = if (input.tipoContrato == TipoContrato.INDEFINIDO)
                                "Seg. Cesantía emp. (2,4%)" else "Seg. Cesantía emp. (3%)"
                            ResultRow(cesantiaLabel, r.cesantiaEmpleador, prefix = "+ ")
                            ResultRow("Mutual de Seguridad", r.mutualMonto, prefix = "+ ")
                            HorizontalDivider(Modifier.padding(vertical = 4.dp))
                            Text("Base de cálculo: $${SimuladorViewModel.formatCLP(r.imponibleTopado)} imponible", style = MaterialTheme.typography.labelSmall)
                            ResultRow("Total Costo Empleador", r.totalCostosEmpleador, bold = true)
                        }

                        // ═══════════════════════════════════════════
                        // FINAL SUMMARY
                        // ═══════════════════════════════════════════
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("RESUMEN FINAL", style = MaterialTheme.typography.labelLarge, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                                Spacer(Modifier.height(12.dp))
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("SUELDO LÍQUIDO", style = MaterialTheme.typography.labelMedium)
                                        Text("$${SimuladorViewModel.formatCLP(r.sueldoLiquido)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                                        Text("Lo que recibe el trabajador", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                                    }
                                    VerticalDivider(modifier = Modifier.height(80.dp).padding(horizontal = 8.dp))
                                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("COSTO EMPRESA", style = MaterialTheme.typography.labelMedium)
                                        Text("$${SimuladorViewModel.formatCLP(r.costoTotalEmpresa)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text("Lo que paga la empresa", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                                    }
                                }
                                
                                comparacion?.let { comp ->
                                    Spacer(Modifier.height(12.dp))
                                    HorizontalDivider()
                                    Spacer(Modifier.height(8.dp))
                                    val ahorro = comp.ahorroMensual
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                        Icon(Icons.Default.TrendingDown, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Ahorro empleador Estructura Optimizada: $${SimuladorViewModel.formatCLP(ahorro)} / mes", 
                                            style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Navigation Buttons
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onNavigateToMulti(r.costoTotalEmpresa) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                                Icon(Icons.Default.Group, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Escalar a N", fontSize = 12.sp)
                            }
                            OutlinedButton(onClick = onNavigateToFiniquito, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Gavel, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Ver Finiquito", fontSize = 12.sp)
                            }
                        }

                        // Values reference
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Valores de Referencia — Vigentes desde ${RC.vigenciaDesde}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    if (isFetchingUtm) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                    } else if (RC.isUtmUpdated) {
                                        InfoBadge("En línea ✓")
                                    } else {
                                        InfoBadge("Valor estimado")
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("UTM: $${SimuladorViewModel.formatCLP(RC.UTM)}", style = MaterialTheme.typography.bodySmall)
                                Text("Sueldo Mínimo: $${SimuladorViewModel.formatCLP(RC.INGRESO_MINIMO)}", style = MaterialTheme.typography.bodySmall)
                                Text("Tope Imponible: $${SimuladorViewModel.formatCLP(RC.TOPE_IMPONIBLE)} (${RC.TOPE_IMPONIBLE_UF} UF)", style = MaterialTheme.typography.bodySmall)
                                Text("SIS: ${RC.SIS_TASA * 100}%  |  Mutual base: ${RC.MUTUAL_BASE * 100}%", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiquidacionBlock(title: String, borderColor: Color, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.fillMaxHeight().width(4.dp).background(borderColor))
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = borderColor)
                Spacer(Modifier.height(4.dp))
                content()
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun MoneyField(label: String, value: Long, onValueChange: (Long) -> Unit, modifier: Modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = if (value > 0) value.toString() else "",
        onValueChange = { text -> onValueChange(text.filter { it.isDigit() }.toLongOrNull() ?: 0L) },
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        prefix = { Text("$") }
    )
}

@Composable
private fun ResultRow(label: String, amount: Long, bold: Boolean = false, negative: Boolean = false, prefix: String = "") {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("$prefix$label", style = MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text("${if (negative) "-" else ""}$${SimuladorViewModel.formatCLP(amount)}", style = MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, color = if (negative) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun InfoBadge(label: String) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.extraSmall, modifier = Modifier.padding(top = 2.dp)) {
        Text(text = label, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}
