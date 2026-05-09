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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiniquitoScreen(
    onBack: () -> Unit,
    viewModel: FiniquitoViewModel = viewModel()
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
                    IconButton(onClick = { /* Reset if needed */ }) {
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
            SectionCard("Datos del Trabajador") {
                MoneyField(
                    label = "Sueldo Base",
                    value = state.sueldoBase,
                    onValueChange = { v -> viewModel.updateState { copy(sueldoBase = v) } }
                )

                Spacer(Modifier.height(8.dp))

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
            }

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

                Spacer(Modifier.height(8.dp))

                Text("Causal de Término", style = MaterialTheme.typography.labelMedium)
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = state.causal.label,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded, { expanded = false }) {
                        CausalFiniquito.entries.forEach { causal ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text(causal.label, fontWeight = FontWeight.Bold)
                                        Text(causal.description, style = MaterialTheme.typography.bodySmall)
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

                if (state.causal == CausalFiniquito.ART_161) {
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
            }

            AnimatedVisibility(result != null) {
                result?.let { r ->
                    ResultCard(r)
                }
            }
        }
    }
}

@Composable
private fun ResultCard(r: FiniquitoResult) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("DESGLOSE FINIQUITO", style = MaterialTheme.typography.labelLarge)
            
            if (r.añosServicio > 0) {
                ResultRow("Indemnización por años (${r.añosCalculados})", r.añosServicio)
            }
            if (r.avisoPrevio > 0) {
                ResultRow("Indemnización sustitutiva aviso previo", r.avisoPrevio)
            }
            ResultRow("Feriado proporcional (${String.format("%.2f", r.diasFeriado)} días)", r.feriadoProporcional)
            ResultRow("Gratificación proporcional", r.gratificacionProporcional)
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("COSTO TOTAL FINIQUITO", fontWeight = FontWeight.ExtraBold)
                Text("$${SimuladorViewModel.formatCLP(r.costoTotal)}", 
                    fontWeight = FontWeight.ExtraBold, 
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 20.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        val selected = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
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
            modifier = Modifier.fillMaxWidth().padding(16.dp),
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
        value = if (value > 0) value.toString() else "",
        onValueChange = { text ->
            val filtered = text.filter { it.isDigit() }
            onValueChange(filtered.toLongOrNull() ?: 0L)
        },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        prefix = { Text("$") }
    )
}

@Composable
private fun ResultRow(label: String, amount: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text("$${SimuladorViewModel.formatCLP(amount)}", style = MaterialTheme.typography.bodyMedium)
    }
}
