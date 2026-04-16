package com.nexo.empresas.dte.ui.dte

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexo.empresas.dte.data.model.ItemDte
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleDteScreen(
    dteId: String,
    onNavigateBack: () -> Unit,
    viewModel: DteViewModel = hiltViewModel()
) {
    val uiState by viewModel.detalleState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

    LaunchedEffect(dteId) {
        viewModel.cargarDetalle(dteId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        uiState.dte?.let { "${it.tipoEnum?.descripcion ?: "DTE"} #${it.folio ?: "-"}" }
                            ?: "Detalle DTE"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Consultar estado SII
                    IconButton(
                        onClick = { viewModel.consultarEstadoSII(dteId) },
                        enabled = !uiState.consultandoEstado
                    ) {
                        if (uiState.consultandoEstado) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = "Consultar estado SII")
                        }
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
                ErrorCard(message = uiState.error!!, onRetry = { viewModel.cargarDetalle(dteId) })
            }
            uiState.dte != null -> {
                val dte = uiState.dte!!
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = paddingValues.calculateTopPadding() + 8.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Encabezado ─────────────────────────────────────────
                    item {
                        Card {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column {
                                        Text(
                                            text = dte.tipoEnum?.descripcion ?: "DTE ${dte.tipoDte}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        dte.folio?.let {
                                            Text("Folio N° $it", style = MaterialTheme.typography.bodyMedium)
                                        }
                                        dte.fechaEmision?.let {
                                            Text(it, style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    EstadoBadge(estado = dte.estadoEnum)
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                // Receptor
                                InfoRow("Receptor", dte.razonSocialReceptor)
                                InfoRow("RUT", dte.rutReceptor)
                                dte.giroReceptor?.let { InfoRow("Giro", it) }
                                dte.direccionReceptor?.let { InfoRow("Dirección", it) }

                                dte.trackId?.let {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    InfoRow("Track ID SII", it)
                                }
                            }
                        }
                    }

                    // ── Items ──────────────────────────────────────────────
                    if (dte.items.isNotEmpty()) {
                        item {
                            Text("Detalle", style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold)
                        }
                        items(dte.items) { item ->
                            ItemDteRow(item = item, formatter = formatter)
                        }
                    }

                    // ── Totales ────────────────────────────────────────────
                    item {
                        Card(colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                TotalRowDetail("Neto", formatter.format(dte.montoNeto))
                                if (dte.montoExento > 0)
                                    TotalRowDetail("Exento", formatter.format(dte.montoExento))
                                TotalRowDetail("IVA (19%)", formatter.format(dte.montoIva))
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                TotalRowDetail("Total", formatter.format(dte.montoTotal), bold = true)
                            }
                        }
                    }

                    // ── Acciones ───────────────────────────────────────────
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Descargar PDF
                            uiState.pdfUrl?.let { url ->
                                Button(
                                    onClick = {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Ver / Descargar PDF")
                                }
                            }

                            // Descargar XML
                            uiState.xmlUrl?.let { url ->
                                OutlinedButton(
                                    onClick = {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Code, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Descargar XML firmado")
                                }
                            }

                            // Reenviar por correo
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT,
                                            "${dte.tipoEnum?.descripcion} N° ${dte.folio}")
                                        putExtra(Intent.EXTRA_TEXT,
                                            "Adjunto ${dte.tipoEnum?.descripcion} N° ${dte.folio}.\n${uiState.pdfUrl ?: ""}")
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Enviar DTE"))
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Enviar por correo")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemDteRow(item: ItemDte, formatter: NumberFormat) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.descripcion, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${item.cantidad} × ${formatter.format(item.precioUnitario)}" +
                    if (item.descuento > 0) " (-${item.descuento}%)" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(formatter.format(item.montoNeto), fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(90.dp)
        )
        Text(text = value, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun TotalRowDetail(label: String, value: String, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}
