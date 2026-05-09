package com.nexo.empresas.presentation.simulador

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiTrabajadorScreen(
    onBack: () -> Unit,
    initialCosto: Long = 0,
    viewModel: MultiTrabajadorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val result by viewModel.result.collectAsState()

    // Cargar costo inicial si se pasa desde el simulador
    LaunchedEffect(initialCosto) {
        if (initialCosto > 0) {
            viewModel.updateState { copy(costoUnitario = initialCosto) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Multi-Trabajador") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.updateState { MultiTrabajadorState() } }) {
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
            SectionCard("Parámetros de Simulación") {
                MoneyField(
                    label = "Costo Mensual Empresa (por trabajador)",
                    value = state.costoUnitario,
                    onValueChange = { v -> viewModel.updateState { copy(costoUnitario = v) } }
                )

                Spacer(Modifier.height(8.dp))

                Text("Número de Trabajadores: ${state.cantidad}", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = state.cantidad.toFloat(),
                    onValueChange = { v -> viewModel.updateState { copy(cantidad = v.toInt()) } },
                    valueRange = 1f..500f,
                    steps = 499
                )
                
                OutlinedTextField(
                    value = state.cantidad.toString(),
                    onValueChange = { text ->
                        val v = text.filter { it.isDigit() }.toIntOrNull() ?: 1
                        viewModel.updateState { copy(cantidad = v.coerceIn(1, 1000)) }
                    },
                    label = { Text("Cantidad exacta") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            var showDetails by remember { mutableStateOf(false) }
            TextButton(onClick = { showDetails = !showDetails }) {
                Icon(Icons.Default.Info, null)
                Spacer(Modifier.width(8.dp))
                Text(if (showDetails) "Ocultar desglose" else "Agregar desglose detallado")
            }

            AnimatedVisibility(showDetails) {
                SectionCard("Desglose Unitario (Opcional)") {
                    MoneyField("Total Imponible", state.imponibleUnitario, { v -> viewModel.updateState { copy(imponibleUnitario = v) } })
                    MoneyField("Total No Imponible", state.noImponibleUnitario, { v -> viewModel.updateState { copy(noImponibleUnitario = v) } })
                    MoneyField("Cotizaciones/Seguros", state.cotizacionesUnitario, { v -> viewModel.updateState { copy(cotizacionesUnitario = v) } })
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
private fun ResultCard(r: MultiTrabajadorResult) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("COSTO MENSUAL TOTAL", style = MaterialTheme.typography.labelLarge)
                Text("$${SimuladorViewModel.formatCLP(r.mensual)}", 
                    style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                ResultRow("Costo Anual (12 meses)", r.anual, bold = true)
                ResultRow("Costo Anual + Gratificación (13 meses)", r.anualConGrat, bold = true)
                
                if (r.imponibleTotal > 0 || r.noImponibleTotal > 0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("Desglose Total", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    ResultRow("Imponible Total", r.imponibleTotal)
                    ResultRow("No Imponible Total", r.noImponibleTotal)
                    ResultRow("Cotizaciones Totales", r.cotizacionesTotal)
                }
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
private fun ResultRow(label: String, amount: Long, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text("$${SimuladorViewModel.formatCLP(amount)}", style = MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}
