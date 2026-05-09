package com.nexo.empresas.presentation.dte

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexo.empresas.dte.data.model.Dte
import com.nexo.empresas.dte.data.model.EstadoDte
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaDtesScreen(
    onNavigateToDetalle: (String) -> Unit,
    onNavigateToEmitir: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onBack: () -> Unit,
    viewModel: DteViewModel = hiltViewModel()
) {
    val uiState by viewModel.listaState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.cargarDtes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Documentos Tributarios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToScanner) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear TED")
                    }
                    IconButton(onClick = { viewModel.cargarDtes(uiState.estadoFiltro) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToEmitir,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Emitir DTE") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            FiltroEstadoRow(
                estadoActual = uiState.estadoFiltro,
                onFiltroSelected = { viewModel.filtrarPorEstado(it) }
            )

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    ErrorCard(
                        message = uiState.error!!,
                        onRetry = { viewModel.cargarDtes() }
                    )
                }
                uiState.dtes.isEmpty() -> {
                    EmptyDtesPlaceholder()
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            uiState.dtes,
                            key = { it.id ?: it.hashCode().toString() }
                        ) { dte ->
                            DteCard(
                                dte = dte,
                                onClick = { dte.id?.let(onNavigateToDetalle) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Filtro de estado ─────────────────────────────────────────────────────────

@Composable
private fun FiltroEstadoRow(
    estadoActual: String?,
    onFiltroSelected: (String?) -> Unit
) {
    val filtros = listOf(null to "Todos") + EstadoDte.entries.map { it.name to it.label }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filtros) { (valor, label) ->
            FilterChip(
                selected = estadoActual == valor,
                onClick = { onFiltroSelected(valor) },
                label = { Text(label) }
            )
        }
    }
}

// ─── Tarjeta DTE ──────────────────────────────────────────────────────────────

@Composable
fun DteCard(dte: Dte, onClick: () -> Unit) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dte.tipoEnum?.descripcion ?: "DTE ${dte.tipoDte}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    dte.folio?.let {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "#$it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = dte.razonSocialReceptor,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = dte.rutReceptor,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatter.format(dte.montoTotal),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                EstadoBadge(estado = dte.estadoEnum)
                dte.fechaEmision?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ─── Badge estado ─────────────────────────────────────────────────────────────

@Composable
fun EstadoBadge(estado: EstadoDte) {
    val (bgColor, textColor) = when (estado) {
        EstadoDte.ACEPTADO -> MaterialTheme.colorScheme.primaryContainer to
                MaterialTheme.colorScheme.onPrimaryContainer
        EstadoDte.RECHAZADO -> MaterialTheme.colorScheme.errorContainer to
                MaterialTheme.colorScheme.onErrorContainer
        EstadoDte.ACEPTADO_REPAROS -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        EstadoDte.ENVIADO -> MaterialTheme.colorScheme.secondaryContainer to
                MaterialTheme.colorScheme.onSecondaryContainer
        EstadoDte.PENDIENTE -> MaterialTheme.colorScheme.surfaceVariant to
                MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = estado.label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

// ─── Placeholders ─────────────────────────────────────────────────────────────

@Composable
private fun EmptyDtesPlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Sin documentos", style = MaterialTheme.typography.titleMedium)
            Text(
                "Emite tu primer DTE con el botón +",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ErrorCard(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Reintentar") }
    }
}
