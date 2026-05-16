package com.nexo.empresas.presentation.simulador

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiniquitoScreen(
    onBack: () -> Unit,
    viewModel: FiniquitoViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val result by viewModel.result.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Simulador Finiquito") },
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

            // ── Sección 1: Datos del trabajador ─────────────────────────────
            SectionCard("Datos del Trabajador") {

                // Tipo de remuneración
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = state.tieneRemuneracionVariable,
                        onCheckedChange = { v ->
                            viewModel.updateState { copy(tieneRemuneracionVariable = v) }
                        }
                    )
                    Text("Remuneración variable (comisiones)")
                }

                if (!state.tieneRemuneracionVariable) {
                    MoneyField(
                        label = "Sueldo Base Mensual",
                        value = state.sueldoBase,
                        onValueChange = { v -> viewModel.updateState { copy(sueldoBase = v) } }
                    )
                } else {
                    MoneyField(
                        label = "Promedio últimos 3 meses",
                        value = state.promedioUltimos3Meses,
                        onValueChange = { v -> viewModel.updateState { copy(promedioUltimos3Meses = v) } }
                    )
                    // Sueldo base igual se necesita para feriado/gratificación
                    MoneyField(
                        label = "Sueldo Base (para feriado y gratificación)",
                        value = state.sueldoBase,
                        onValueChange = { v -> viewModel.updateState { copy(sueldoBase = v) } }
                    )
                }

                DatePickerField(
                    label = "Fecha Inicio Contrato",
                    date = state.fechaInicio,
                    onDateSelected = { d -> viewModel.updateState { copy(fechaInicio = d) } }
                )

                DatePickerField(
                    label = "Fecha Término Contrato",
                    date = state.fechaTermino,
                    onDateSelected = { d -> viewModel.updateState { copy(fechaTermino = d) } }
                )

                // Días trabajados en el mes de término — calculado automáticamente desde fechaTermino
                val diasUltimoMes = state.fechaTermino.dayOfMonth
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Días trabajados en el mes de término",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            "$diasUltimoMes días",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // ── Sección 2: Causal y contrato ────────────────────────────────
            SectionCard("Causal y Contrato") {

                Text("Tipo de Contrato", style = MaterialTheme.typography.labelMedium)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    TipoContratoFiniquito.entries.forEachIndexed { idx, tipo ->
                        SegmentedButton(
                            selected = state.tipoContrato == tipo,
                            onClick = { viewModel.updateState { copy(tipoContrato = tipo) } },
                            shape = SegmentedButtonDefaults.itemShape(idx, TipoContratoFiniquito.entries.size)
                        ) { Text(tipo.label, fontSize = 11.sp) }
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text("Causal de Término", style = MaterialTheme.typography.labelMedium)
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = "${state.causal.label} — ${state.causal.description}",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(expanded, { expanded = false }) {
                        CausalFiniquito.entries.forEach { causal ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(causal.label, fontWeight = FontWeight.Bold)
                                        Text(
                                            causal.description,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        if (causal.generaIAS) {
                                            Text(
                                                "✓ Genera indemnización por años",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.updateState { copy(causal = causal) }
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Aviso previo solo aplica en Art. 161
                AnimatedVisibility(state.causal.generaAvisoPrevio) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = state.avisoDado,
                            onCheckedChange = { v -> viewModel.updateState { copy(avisoDado = v) } }
                        )
                        Text("Se dio aviso con 30 días de anticipación")
                    }
                }

                // Advertencia Art. 160
                AnimatedVisibility(!state.causal.generaIAS && !state.causal.generaAvisoPrevio
                        && state.causal == CausalFiniquito.ART_160) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Esta causal no genera indemnizaciones (solo feriado y días adeudados)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // ── Sección 3: Vacaciones ────────────────────────────────────────
            SectionCard("Vacaciones") {
                Text(
                    "¿El trabajador tiene vacaciones pendientes sin tomar?",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Opción: SÍ tiene vacaciones pendientes
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = state.tieneVacacionesPendientes,
                        onClick = {
                            viewModel.updateState {
                                copy(tieneVacacionesPendientes = true)
                            }
                        }
                    )
                    Text("Sí, tiene días pendientes")
                }

                // Campo de días: solo visible si respondió SÍ
                AnimatedVisibility(visible = state.tieneVacacionesPendientes) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Ingresa los días hábiles de vacaciones ya tomadas " +
                                    "(se restan del total acumulado)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IntegerField(
                            label = "Días de vacaciones ya tomadas",
                            value = state.diasVacacionesTomadas,
                            onValueChange = { v ->
                                viewModel.updateState { copy(diasVacacionesTomadas = v) }
                            },
                            range = 0..999
                        )
                    }
                }

                // Opción: NO tiene vacaciones pendientes
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = !state.tieneVacacionesPendientes,
                        onClick = {
                            viewModel.updateState {
                                copy(
                                    tieneVacacionesPendientes = false,
                                    diasVacacionesTomadas = 0
                                )
                            }
                        }
                    )
                    Text("No, ya las tomó todas")
                }
            }

            // ── Sección 4: Gratificación ─────────────────────────────────────
            SectionCard("Gratificación") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = state.gratificacionYaPagadaMensual,
                        onCheckedChange = { v ->
                            viewModel.updateState { copy(gratificacionYaPagadaMensual = v) }
                        }
                    )
                    Column {
                        Text("La empresa pagó gratificación mensualmente")
                        Text(
                            "Si está marcado, no se adeuda gratificación en el finiquito",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Resultado ────────────────────────────────────────────────────
            AnimatedVisibility(result != null) {
                result?.let { r -> ResultCard(r) }
            }

            // Aviso legal
            Text(
                "* Este simulador es referencial. Los montos reales pueden variar según " +
                        "remuneraciones variables, cláusulas contractuales y la UF vigente al momento " +
                        "del pago. Siempre valide ante la Dirección del Trabajo o un profesional.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Tarjeta de resultado ─────────────────────────────────────────────────────
@Composable
private fun ResultCard(r: FiniquitoResult) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "DESGLOSE FINIQUITO",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            // Remuneración proporcional del último mes
            if (r.remuneracionUltimoMes > 0) {
                ResultRow(
                    label = "Remuneración proporcional (${r.diasUltimoMes} días)",
                    amount = r.remuneracionUltimoMes
                )
            }

            // Indemnización por años de servicio
            if (r.indemnizacionAnios > 0) {
                val topeLabel = if (r.topadaPor90UF) " — base topada a 90 UF" else ""
                ResultRow(
                    label = "Indemnización años de servicio (${r.aniosReconocidos} años$topeLabel)",
                    amount = r.indemnizacionAnios
                )
            }

            // Aviso previo
            if (r.indemnizacionAvisoPrevio > 0) {
                ResultRow(
                    label = "Indemnización sustitutiva aviso previo",
                    amount = r.indemnizacionAvisoPrevio
                )
            }

            // Feriado proporcional
            if (r.feriadoProporcional > 0) {
                ResultRow(
                    label = buildString {
                        append("Feriado proporcional ")
                        append("(${String.format("%.1f", r.diasFeriadoBruto)} acum.")
                        append(" − ${r.diasFeriadoBruto.toInt() - r.diasFeriadoNeto.toInt().coerceAtMost(r.diasFeriadoBruto.toInt())} tomados")
                        append(" = ${String.format("%.1f", r.diasFeriadoNeto)} días)")
                    },
                    amount = r.feriadoProporcional
                )
            }

            // Gratificación
            if (r.gratificacionProporcional > 0) {
                ResultRow(
                    label = "Gratificación proporcional (${r.mesesGratificacion} meses, Art. 50)",
                    amount = r.gratificacionProporcional
                )
            }

            // Sin conceptos
            if (r.costoTotal == 0L) {
                Text(
                    "No se encontraron conceptos adeudados con los datos ingresados.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("COSTO TOTAL FINIQUITO", fontWeight = FontWeight.ExtraBold)
                Text(
                    "\$${formatCLP(r.costoTotal)}",
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 20.sp
                )
            }
        }
    }
}

// ── Componentes reutilizables ─────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = date
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        val selected = Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(selected)
                    }
                    showDialog = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    OutlinedTextField(
        value = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.CalendarMonth, null)
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun MoneyField(
    label: String,
    value: Long,
    onValueChange: (Long) -> Unit
) {
    OutlinedTextField(
        value = if (value > 0L) value.toString() else "",
        onValueChange = { text ->
            val filtered = text.filter { it.isDigit() }
            onValueChange(filtered.toLongOrNull() ?: 0L)
        },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        prefix = { Text("\$") }
    )
}

@Composable
private fun IntegerField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange = 0..999
) {
    OutlinedTextField(
        value = if (value > 0) value.toString() else "",
        onValueChange = { text ->
            val filtered = text.filter { it.isDigit() }
            val v = filtered.toIntOrNull() ?: 0
            if (v in range) onValueChange(v) else if (filtered.isEmpty()) onValueChange(0)
        },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
private fun ResultRow(label: String, amount: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "\$${formatCLP(amount)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** Formatea un Long como pesos chilenos con separador de miles */
fun formatCLP(amount: Long): String =
    String.format("%,d", amount).replace(',', '.')
