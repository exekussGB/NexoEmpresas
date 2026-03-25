package cl.nexo.empresas.presentation.simulador

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.nexo.empresas.data.model.*
import cl.nexo.empresas.data.model.RemuneracionesChile as RC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimuladorScreen(
    onBack: () -> Unit,
    viewModel: SimuladorViewModel = viewModel()
) {
    val input by viewModel.input.collectAsState()
    val result by viewModel.result.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Simulador Contratación") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
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
            // SECCIÓN: DATOS DEL CANDIDATO
            // ═══════════════════════════════════════════
            SectionCard("Pretensión de Renta") {
                MoneyField(
                    label = "Sueldo Base *",
                    value = input.sueldoBase,
                    onValueChange = { v -> viewModel.updateInput { copy(sueldoBase = v) } }
                )
            }

            SectionCard("Contrato y Previsión") {
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

                // AFP
                Text("AFP", style = MaterialTheme.typography.labelMedium)
                var afpExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = afpExpanded,
                    onExpandedChange = { afpExpanded = it }
                ) {
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

                // Gratificación
                Text("Gratificación Legal", style = MaterialTheme.typography.labelMedium)
                var gratExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = gratExpanded,
                    onExpandedChange = { gratExpanded = it }
                ) {
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

            // Otros haberes
            SectionCard("Otros Haberes (opcional)") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoneyField("Comisiones", input.comisiones,
                        { v -> viewModel.updateInput { copy(comisiones = v) } },
                        Modifier.weight(1f))
                    MoneyField("Bonos Imp.", input.bonosImponibles,
                        { v -> viewModel.updateInput { copy(bonosImponibles = v) } },
                        Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoneyField("Hrs. Extra", input.horasExtras,
                        { v -> viewModel.updateInput { copy(horasExtras = v) } },
                        Modifier.weight(1f))
                    MoneyField("Colación", input.colacion,
                        { v -> viewModel.updateInput { copy(colacion = v) } },
                        Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoneyField("Movilización", input.movilizacion,
                        { v -> viewModel.updateInput { copy(movilizacion = v) } },
                        Modifier.weight(1f))
                    MoneyField("Viáticos", input.viaticos,
                        { v -> viewModel.updateInput { copy(viaticos = v) } },
                        Modifier.weight(1f))
                }
                MoneyField("Bonos No Imponibles", input.bonosNoImponibles,
                    { v -> viewModel.updateInput { copy(bonosNoImponibles = v) } })
            }

            // Mutual adicional
            SectionCard("Mutual de Seguridad") {
                Text(
                    "Tasa base: 0,93%. Agrega tasa adicional según actividad económica (0% a 3,4%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = if (input.tasaMutualAdicional > 0)
                        input.tasaMutualAdicional.toString() else "",
                    onValueChange = { text ->
                        val v = text.toDoubleOrNull() ?: 0.0
                        viewModel.updateInput { copy(tasaMutualAdicional = v.coerceIn(0.0, 3.4)) }
                    },
                    label = { Text("Tasa adicional (%)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    suffix = { Text("%") }
                )
            }

            // ═══════════════════════════════════════════
            // SECCIÓN: RESULTADOS
            // ═══════════════════════════════════════════
            AnimatedVisibility(result != null) {
                result?.let { r ->
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                        // ── COSTO TOTAL ──
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("COSTO TOTAL EMPRESA",
                                    style = MaterialTheme.typography.labelLarge)
                                Text(
                                    "\$${SimuladorViewModel.formatCLP(r.costoTotalEmpresa)}",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("Líquido trabajador",
                                    style = MaterialTheme.typography.labelMedium)
                                Text(
                                    "\$${SimuladorViewModel.formatCLP(r.sueldoLiquido)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }

                        // ── Composición visual ──
                        val totalRef = r.costoTotalEmpresa.toFloat()
                        if (totalRef > 0 && r.sueldoLiquido > 0) {
                            val liquidoPct = (r.sueldoLiquido / totalRef).coerceAtLeast(0.01f)
                            val descPct = (r.totalDescuentosTrabajador / totalRef).coerceAtLeast(0.01f)
                            val empPct = (r.totalCostosEmpleador / totalRef).coerceAtLeast(0.01f)
                            Row(
                                modifier = Modifier.fillMaxWidth().height(24.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.shapes.small
                                    )
                            ) {
                                Box(Modifier.weight(liquidoPct).fillMaxHeight()
                                    .background(
                                        MaterialTheme.colorScheme.tertiary,
                                        MaterialTheme.shapes.small
                                    ))
                                Box(Modifier.weight(descPct).fillMaxHeight()
                                    .background(
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                        MaterialTheme.shapes.small
                                    ))
                                Box(Modifier.weight(empPct).fillMaxHeight()
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        MaterialTheme.shapes.small
                                    ))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                LegendDot("Líquido", MaterialTheme.colorScheme.tertiary)
                                LegendDot("Descuentos", MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                                LegendDot("Empleador", MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            }
                        }

                        // ── Haberes ──
                        ResultSection("Haberes Imponibles") {
                            ResultRow("Sueldo Base", r.sueldoBase)
                            ResultRow("Gratificación", r.gratificacion)
                            if (r.comisiones > 0) ResultRow("Comisiones", r.comisiones)
                            if (r.bonosImponibles > 0) ResultRow("Bonos Imponibles", r.bonosImponibles)
                            if (r.horasExtras > 0) ResultRow("Horas Extra", r.horasExtras)
                            Divider()
                            ResultRow("Total Imponible", r.totalImponible, bold = true)
                            if (r.excedeTopeImponible) {
                                Text(
                                    "⚠️ Excede tope imponible (\$${SimuladorViewModel.formatCLP(RC.TOPE_IMPONIBLE)}). " +
                                            "Se usa topado: \$${SimuladorViewModel.formatCLP(r.imponibleTopado)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        if (r.totalNoImponible > 0) {
                            ResultSection("Haberes No Imponibles") {
                                if (r.colacion > 0) ResultRow("Colación", r.colacion)
                                if (r.movilizacion > 0) ResultRow("Movilización", r.movilizacion)
                                if (r.viaticos > 0) ResultRow("Viáticos", r.viaticos)
                                if (r.bonosNoImponibles > 0) ResultRow("Bonos No Imp.", r.bonosNoImponibles)
                                Divider()
                                ResultRow("Total No Imponible", r.totalNoImponible, bold = true)
                            }
                        }

                        // ── Descuentos trabajador ──
                        ResultSection("Descuentos Trabajador") {
                            ResultRow("AFP ${r.afpNombre}", r.afpMonto, negative = true)
                            ResultRow(r.saludDetalle, r.saludMonto, negative = true)
                            if (r.cesantiaTrabajador > 0)
                                ResultRow("Seg. Cesantía (0,6%)", r.cesantiaTrabajador, negative = true)
                            if (r.impuestoUnico > 0) {
                                ResultRow(
                                    "Impuesto Único (${String.format("%.1f", r.tasaEfectivaImpuesto)}%)",
                                    r.impuestoUnico,
                                    negative = true
                                )
                            }
                            Divider()
                            ResultRow("Total Descuentos", r.totalDescuentosTrabajador,
                                bold = true, negative = true)
                        }

                        // ── Costos empleador ──
                        ResultSection("Costos Empleador (adicionales)") {
                            ResultRow("SIS (1,54%)", r.sisMonto)
                            ResultRow("Seg. Cesantía Empleador", r.cesantiaEmpleador)
                            ResultRow("Mutual de Seguridad", r.mutualMonto)
                            Divider()
                            ResultRow("Total Costos Empleador", r.totalCostosEmpleador, bold = true)
                        }

                        // ── Valores de referencia ──
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Valores de Referencia — Marzo 2026",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text("UTM: \$${SimuladorViewModel.formatCLP(RC.UTM)}", style = MaterialTheme.typography.bodySmall)
                                Text("Sueldo Mínimo: \$${SimuladorViewModel.formatCLP(RC.INGRESO_MINIMO)}", style = MaterialTheme.typography.bodySmall)
                                Text("Tope Imponible: \$${SimuladorViewModel.formatCLP(RC.TOPE_IMPONIBLE)} (${RC.TOPE_IMPONIBLE_UF} UF)", style = MaterialTheme.typography.bodySmall)
                                Text("SIS: ${RC.SIS_TASA * 100}%  |  Mutual base: ${RC.MUTUAL_BASE * 100}%", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Spacer(Modifier.height(32.dp))
                    }
                }
            }

            // Mensaje inicial
            if (result == null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Calculate,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Ingresa la pretensión de renta del candidato para ver el desglose completo del costo de contratación.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
// COMPONENTES REUTILIZABLES
// ═══════════════════════════════════════════

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun ResultSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    )) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun MoneyField(
    label: String,
    value: Long,
    onValueChange: (Long) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = if (value > 0) value.toString() else "",
        onValueChange = { text ->
            val filtered = text.filter { it.isDigit() }
            onValueChange(filtered.toLongOrNull() ?: 0L)
        },
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        prefix = { Text("$") }
    )
}

@Composable
private fun ResultRow(
    label: String,
    amount: Long,
    bold: Boolean = false,
    negative: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            "${if (negative) "-" else ""}\$${SimuladorViewModel.formatCLP(amount)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = if (negative) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            Modifier.size(8.dp)
                .background(color, MaterialTheme.shapes.extraSmall)
        )
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}