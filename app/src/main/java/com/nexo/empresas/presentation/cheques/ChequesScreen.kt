package com.nexo.empresas.presentation.cheques

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexo.empresas.data.model.Cheque
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChequesScreen(
    onBack: () -> Unit,
    viewModel: ChequesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cheques") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Pendientes (${state.pendientes.size})", modifier = Modifier.padding(vertical = 12.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Cobrados (${state.cobrados.size})", modifier = Modifier.padding(vertical = 12.dp))
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                    Text("Rechazados (${state.rechazados.size})", modifier = Modifier.padding(vertical = 12.dp))
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val cheques = when (selectedTab) {
                    0 -> state.pendientes
                    1 -> state.cobrados
                    else -> state.rechazados
                }

                if (cheques.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay cheques", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(cheques, key = { it.id }) { cheque ->
                            ChequeCard(
                                cheque = cheque,
                                isOwner = viewModel.isOwner(),
                                onCobrado = { viewModel.actualizarEstado(cheque.id, "cobrado") },
                                onRechazado = { viewModel.actualizarEstado(cheque.id, "rechazado") },
                                onReactivar = { viewModel.actualizarEstado(cheque.id, "pendiente") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChequeCard(
    cheque: Cheque,
    isOwner: Boolean,
    onCobrado: () -> Unit,
    onRechazado: () -> Unit,
    onReactivar: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("es", "CL"))
    val fechaCobro = try { LocalDate.parse(cheque.fechaCobro).format(formatter) } catch (e: Exception) { cheque.fechaCobro }

    val hoy = LocalDate.now()
    val diasParaCobro = try {
        val fecha = LocalDate.parse(cheque.fechaCobro)
        java.time.temporal.ChronoUnit.DAYS.between(hoy, fecha)
    } catch (e: Exception) { Long.MAX_VALUE }

    val estaProximo = cheque.estado == "pendiente" && diasParaCobro in 0..7

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Cheque Nº ${cheque.numeroCheque}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    cheque.banco?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Text("Cobrar: $fechaCobro", style = MaterialTheme.typography.bodySmall)
                    if (estaProximo) {
                        Text(
                            "⚠ ${if (diasParaCobro == 0L) "Hoy" else "En $diasParaCobro días"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFF57F17),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    formatMonto(cheque.monto),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isOwner) {
                when (cheque.estado) {
                    "pendiente" -> {
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onCobrado,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                            ) { Text("Cobrado") }
                            OutlinedButton(onClick = onRechazado, modifier = Modifier.weight(1f)) {
                                Text("Rechazado", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    "rechazado" -> {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = onReactivar, modifier = Modifier.fillMaxWidth()) {
                            Text("Reactivar")
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

private fun formatMonto(monto: Long): String {
    val formatted = "%,d".format(monto).replace(",", ".")
    return "$$formatted"
}
