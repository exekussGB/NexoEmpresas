package cl.nexo.empresas.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import cl.nexo.empresas.data.model.CuentaDashboard
import java.text.NumberFormat
import java.util.Locale

private fun Long.formatCLP(): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CL")).format(this)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onBack: () -> Unit,
    onVerGraficos: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val totales by viewModel.totales.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.loadTotales() }) {
                        Text("Actualizar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error ?: "Error desconocido",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadTotales() }) {
                            Text("Reintentar")
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // --- 3 tarjetas resumen ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ResumenCard(
                                modifier = Modifier.weight(1f),
                                emoji = "💰",
                                label = "Por Cobrar",
                                amount = totales?.totalPorCobrar ?: 0L,
                                color = Color(0xFF2E7D32)
                            )
                            ResumenCard(
                                modifier = Modifier.weight(1f),
                                emoji = "💳",
                                label = "Por Pagar",
                                amount = totales?.totalPorPagar ?: 0L,
                                color = Color(0xFFC62828)
                            )
                            ResumenCard(
                                modifier = Modifier.weight(1f),
                                emoji = "🔷",
                                label = "Cheques",
                                amount = totales?.totalChequesPendientes ?: 0L,
                                color = Color(0xFF1565C0)
                            )
                        }

                        // --- Lista de cuentas corrientes ---
                        if (!totales?.cuentas.isNullOrEmpty()) {
                            Text(
                                text = "Cuentas Corrientes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            totales!!.cuentas.forEach { cuenta ->
                                CuentaCard(cuenta = cuenta)
                            }
                        }

                        // --- Botón Ver Gráficos ---
                        Button(
                            onClick = onVerGraficos,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ver Gráficos")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumenCard(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    amount: Long,
    color: Color
) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = amount.formatCLP(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CuentaCard(cuenta: CuentaDashboard) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = cuenta.nombre,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ingresos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = cuenta.ingresos.formatCLP(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1565C0),
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Egresos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = cuenta.egresos.formatCLP(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC62828),
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Saldo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = cuenta.saldo.formatCLP(),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (cuenta.saldo >= 0) Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
