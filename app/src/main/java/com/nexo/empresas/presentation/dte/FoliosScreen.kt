package com.nexoempresas.dte.ui.dte

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexoempresas.dte.data.model.Folio

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoliosScreen(
    empresaId: String,
    onNavigateBack: () -> Unit,
    viewModel: DteViewModel = hiltViewModel()
) {
    val uiState by viewModel.foliosState.collectAsStateWithLifecycle()

    LaunchedEffect(empresaId) {
        viewModel.cargarFolios(empresaId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Folios disponibles") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.cargarFolios(empresaId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                ErrorCard(uiState.error!!, onRetry = { viewModel.cargarFolios(empresaId) })
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = paddingValues.calculateTopPadding() + 8.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Estos son los rangos de folios (CAF) autorizados por el SII para tu empresa.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    if (uiState.folios.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    "Sin folios registrados. Solicita un CAF en palena.sii.cl",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(uiState.folios) { folio ->
                            FolioCard(folio = folio)
                        }
                    }

                    item {
                        // Instrucciones para solicitar más folios
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "¿Necesitas más folios?",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "1. Ingresa a palena.sii.cl (producción) o maullin.sii.cl (certificación).\n" +
                                    "2. Ve a Factura Electrónica → Solicitar Folios.\n" +
                                    "3. Descarga el archivo CAF (.xml).\n" +
                                    "4. Entrega el archivo al administrador para cargarlo en el sistema.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Tarjeta de folio ─────────────────────────────────────────────────────────

@Composable
private fun FolioCard(folio: Folio) {
    val porcentajeUsado = if (folio.folioHasta > folio.folioDesde) {
        (folio.folioActual - folio.folioDesde).toFloat() /
                (folio.folioHasta - folio.folioDesde).toFloat()
    } else 1f

    val alerta = folio.disponibles < 10

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (alerta) CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ) else CardDefaults.cardColors()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = folio.tipoEnum?.descripcion ?: "Tipo ${folio.tipoDte}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Código ${folio.tipoDte}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (alerta) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "¡Pocos folios!",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                FolioInfoItem("Desde", folio.folioDesde.toString())
                FolioInfoItem("Hasta", folio.folioHasta.toString())
                FolioInfoItem("Actual", folio.folioActual.toString())
                FolioInfoItem("Disponibles", folio.disponibles.toString(), highlight = true)
            }

            Spacer(Modifier.height(8.dp))

            // Barra de progreso de uso
            LinearProgressIndicator(
                progress = { porcentajeUsado },
                modifier = Modifier.fillMaxWidth(),
                color = if (alerta) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${(porcentajeUsado * 100).toInt()}% utilizado",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun FolioInfoItem(label: String, value: String, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
