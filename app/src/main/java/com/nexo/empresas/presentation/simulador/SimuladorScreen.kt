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
    viewModel: SimuladorViewModel = viewModel()
) {
    val input by viewModel.input.collectAsState()
    val result by viewModel.result.collectAsState()
    val isFetchingUtm by viewModel.isFetchingUtm.collectAsState()
    val manualFields by viewModel.manualFields.collectAsState()
    val comparacion by viewModel.comparacion.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Asegurar que siempre esté en modo DESDE_LIQUIDO
    LaunchedEffect(Unit) {
        if (input.modoCalculo != ModoCalculo.DESDE_LIQUIDO) {
            viewModel.updateInput { copy(modoCalculo = ModoCalculo.DESDE_LIQUIDO) }
        }
    }

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
            // SECCIÓN: DATOS DEL CANDIDATO
            // ═══════════════════════════════════════════
            SectionCard("Pretensión de Renta") {
                OutlinedTextField(
                    value = input.nombreCandidato,
                    onValueChange = { v -> viewModel.updateInput { copy(nombreCandidato = v) } },
                    label = { Text("Nombre del Candidato / Empresa") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                MoneyField(
                    label = "¿Cuánto quiere ganar al bolsillo? *",
                    value = input.sueldoLiquidoDeseado,
                    onValueChange = { v -> viewModel.updateInput { copy(sueldoLiquidoDeseado = v) } }
                )
                
                // Sueldo Base Fixed
                OutlinedTextField(
                    value = "$${SimuladorViewModel.formatCLP(RC.INGRESO_MINIMO)}",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sueldo Base (Mínimo Legal)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Lock, "Fijo") },
                    supportingText = { Text("Protegido por ley: ingreso mínimo mensual") }
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
                Text(
                    "Tope legal: $213.354 (4.75 × IMM mensual)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // OTROS HABERES
            SectionCard("Otros Haberes (opcional)") {
                // Subsection A: Haberes Imponibles
                Text("Haberes Imponibles", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoneyField("Comisiones", input.comisiones,
                        { v -> viewModel.updateInput { copy(comisiones = v) } },
                        Modifier.weight(1f))
                    MoneyField("Bonos Imp.", input.bonosImponibles,
                        { v -> viewModel.updateInput { copy(bonosImponibles = v) } },
                        Modifier.weight(1f))
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
                    result?.let { r ->
                        val valorHora = if (r.sueldoBase > 0) (r.sueldoBase / 30.0 / 8.0 * 1.5).roundToLong() else 0L
                        Text(
                            "Valor hora extra: \$${SimuladorViewModel.formatCLP(valorHora)}  |  Total: \$${SimuladorViewModel.formatCLP(r.horasExtras)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Subsection B: Haberes No Imponibles
                Text("Haberes No Imponibles", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        MoneyField("Colación", input.colacion,
                            { v -> viewModel.updateInput { copy(colacion = v) } })
                        if ("colacion" !in manualFields && input.colacion > 0) {
                            InfoBadge()
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        MoneyField("Movilización", input.movilizacion,
                            { v -> viewModel.updateInput { copy(movilizacion = v) } })
                        if ("movilizacion" !in manualFields && input.movilizacion > 0) {
                            InfoBadge()
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoneyField("Viáticos", input.viaticos,
                        { v -> viewModel.updateInput { copy(viaticos = v) } },
                        Modifier.weight(1f))
                    MoneyField("Desgaste Herr.", input.desgasteHerramientas,
                        { v -> viewModel.updateInput { copy(desgasteHerramientas = v) } },
                        Modifier.weight(1f))
                }
                Column {
                    MoneyField("Otros no imponibles", input.bonosNoImponibles,
                        { v -> viewModel.updateInput { copy(bonosNoImponibles = v) } })
                    if ("otrosNoImponibles" !in manualFields && input.bonosNoImponibles > 0) {
                        InfoBadge()
                    }
                }
            }

            // Mutual adicional
            SectionCard("Mutual de Seguridad") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = "0.93%",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tasa base") },
                        modifier = Modifier.weight(1f)
                    )
                    Text("+", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = if (input.tasaMutualAdicional > 0)
                            input.tasaMutualAdicional.toString() else "",
                        onValueChange = { text ->
                            val v = text.toDoubleOrNull() ?: 0.0
                            viewModel.updateInput { copy(tasaMutualAdicional = v.coerceIn(0.0, 3.4)) }
                        },
                        label = { Text("Adicional (%)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        suffix = { Text("%") }
                    )
                }
            }

            SectionCard("Otros Descuentos") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoneyField("Anticipo", input.anticipo,
                        { v -> viewModel.updateInput { copy(anticipo = v) } },
                        Modifier.weight(1f))
                    MoneyField("Préstamo empresa", input.prestamoEmpresa,
                        { v -> viewModel.updateInput { copy(prestamoEmpresa = v) } },
                        Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = input.otrosDescuentosLabel,
                        onValueChange = { v -> viewModel.updateInput { copy(otrosDescuentosLabel = v) } },
                        label = { Text("Concepto") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    MoneyField("Monto", input.otrosDescuentos,
                        { v -> viewModel.updateInput { copy(otrosDescuentos = v) } },
                        Modifier.weight(1f))
                }
            }

            // ═══════════════════════════════════════════
            // SECCIÓN: RESULTADOS (LIQUIDACIÓN STYLE)
            // ═══════════════════════════════════════════
            AnimatedVisibility(result != null) {
                result?.let { r ->
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                        // Composición visual
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
                                    .background(Color(0xFF4CAF50), MaterialTheme.shapes.small))
                                Box(Modifier.weight(descPct).fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.error, MaterialTheme.shapes.small))
                                Box(Modifier.weight(empPct).fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small))
                            }
                        }

                        // BLOCK A — INGRESOS
                        PayslipBlock("INGRESOS", Color(0xFF4CAF50)) {
                            ResultRow("Sueldo Base", r.sueldoBase, prefix = "+ ")
                            ResultRow("Gratificación", r.gratificacion, prefix = "+ ")
                            if (r.horasExtras > 0) ResultRow("Horas Extra", r.horasExtras, prefix = "+ ")
                            if (r.comisiones > 0) ResultRow("Comisiones", r.comisiones, prefix = "+ ")
                            if (r.bonosImponibles > 0) ResultRow("Bonos Imponibles", r.bonosImponibles, prefix = "+ ")
                            HorizontalDivider()
                            ResultRow("Total Imponible", r.totalImponible, bold = true)
                            if (r.colacion > 0) ResultRow("Colación", r.colacion, prefix = "+ ")
                            if (r.movilizacion > 0) ResultRow("Movilización", r.movilizacion, prefix = "+ ")
                            if (r.viaticos > 0) ResultRow("Viáticos", r.viaticos, prefix = "+ ")
                            if (r.desgasteHerramientas > 0) ResultRow("Desgaste Herramientas", r.desgasteHerramientas, prefix = "+ ")
                            if (r.bonosNoImponibles > 0) ResultRow("Otros no imponibles", r.bonosNoImponibles, prefix = "+ ")
                            HorizontalDivider()
                            ResultRow("Total Haberes", r.totalHaberes, bold = true)
                        }

                        // BLOCK B — DESCUENTOS TRABAJADOR
                        PayslipBlock("DESCUENTOS TRABAJADOR", MaterialTheme.colorScheme.error) {
                            ResultRow("AFP ${r.afpNombre}", r.afpMonto, negative = true, prefix = "- ")
                            ResultRow("Salud ${r.saludDetalle}", r.saludMonto, negative = true, prefix = "- ")
                            if (r.cesantiaTrabajador > 0) ResultRow("Seg. Cesantía (0,6%)", r.cesantiaTrabajador, negative = true, prefix = "- ")
                            if (r.impuestoUnico > 0) ResultRow("Impuesto Único (${String.format("%.1f", r.tasaEfectivaImpuesto)}%)", r.impuestoUnico, negative = true, prefix = "- ")
                            if (r.anticipo > 0) ResultRow("Anticipo", r.anticipo, negative = true, prefix = "- ")
                            if (r.prestamoEmpresa > 0) ResultRow("Préstamo empresa", r.prestamoEmpresa, negative = true, prefix = "- ")
                            if (r.otrosDescuentos > 0) ResultRow(r.otrosDescuentosLabel, r.otrosDescuentos, negative = true, prefix = "- ")
                            HorizontalDivider()
                            ResultRow("Total Descuentos", r.totalDescuentosTrabajador, bold = true, negative = true)
                        }

                        // BLOCK C — COSTO ADICIONAL EMPLEADOR
                        PayslipBlock("COSTO ADICIONAL EMPLEADOR", MaterialTheme.colorScheme.primary) {
                            ResultRow("SIS (1,54%)", r.sisMonto, prefix = "+ ")
                            val cesantiaLabel = if (input.tipoContrato == TipoContrato.INDEFINIDO)
                                "Seg. Cesantía emp. (2,4%)" else "Seg. Cesantía emp. (3%)"
                            ResultRow(cesantiaLabel, r.cesantiaEmpleador, prefix = "+ ")
                            ResultRow("Mutual de Seguridad", r.mutualMonto, prefix = "+ ")
                            HorizontalDivider()
                            ResultRow("Total Costo Empleador", r.totalCostosEmpleador, bold = true)
                        }

                        // FINAL SUMMARY
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("SUELDO LÍQUIDO", style = MaterialTheme.typography.labelMedium)
                                        Text(
                                            "\$${SimuladorViewModel.formatCLP(r.sueldoLiquido)}",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                        Text("Lo que recibe el trabajador", style = MaterialTheme.typography.labelSmall)
                                    }
                                    VerticalDivider(modifier = Modifier.height(60.dp).padding(horizontal = 8.dp))
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("COSTO TOTAL", style = MaterialTheme.typography.labelMedium)
                                        Text(
                                            "\$${SimuladorViewModel.formatCLP(r.costoTotalEmpresa)}",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text("Lo que paga la empresa", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                
                                comparacion?.let { comp ->
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Si pagara todo imponible:", style = MaterialTheme.typography.labelSmall)
                                            Text("$${SimuladorViewModel.formatCLP(comp.costoTodoImponible)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        }
                                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                            Text("Ahorro empleador:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
                                            Text("$${SimuladorViewModel.formatCLP(comp.ahorroMensual)} / mes", 
                                                style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                                        }
                                    }
                                }
                            }
                        }

                        // Acciones adicionales
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onNavigateToMulti(r.costoTotalEmpresa) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Default.Group, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Escalar a N personas", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = onNavigateToFiniquito,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Gavel, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Ver Finiquito", fontSize = 11.sp)
                            }
                        }

                        // TODO: These values should be fetched from an API or remote config.
                        // Currently hardcoded in RemuneracionesChile object.
                        // Suggested solution: use a RemoteConfig or a Supabase table "parametros_legales"
                        // with columns: nombre, valor, vigencia_desde. Update quarterly.
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Valores de Referencia — Vigentes desde ${RC.VIGENCIA_DESDE}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f))
                                    if (isFetchingUtm) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                    } else if (RC.isUtmUpdated) {
                                        InfoBadge(label = "Actualizado")
                                    }
                                }
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
                            "Ingresa la pretensión líquida del candidato para ver el desglose completo del costo de contratación.",
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
private fun PayslipBlock(
    title: String,
    borderColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.fillMaxHeight().width(4.dp).background(borderColor))
            Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = borderColor)
                content()
            }
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
    negative: Boolean = false,
    prefix: String = ""
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "$prefix$label",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            "${if (negative) "-" else ""}\$${SimuladorViewModel.formatCLP(amount)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = if (negative) MaterialTheme.colorScheme.error
            else if (bold) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun InfoBadge(label: String = "Sugerido") {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.extraSmall,
        modifier = Modifier.padding(top = 2.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
