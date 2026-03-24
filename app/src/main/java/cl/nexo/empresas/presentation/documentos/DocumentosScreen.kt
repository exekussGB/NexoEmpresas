package cl.nexo.empresas.presentation.documentos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cl.nexo.empresas.data.model.Documento
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentosScreen(
    tipo: String,           // "ingreso" o "egreso"
    onBack: () -> Unit,
    onAddDocumento: () -> Unit,
    viewModel: DocumentosViewModel = hiltViewModel()
) {
    LaunchedEffect(tipo) { viewModel.init(tipo) }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showMarcarPagadoDialog by remember { mutableStateOf<Documento?>(null) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    val titulo = if (tipo == "ingreso") "Cuentas por Cobrar" else "Cuentas por Pagar"
    val colorMonto = if (tipo == "ingreso") Color(0xFF2E7D32) else Color(0xFFC62828)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (viewModel.isOwner()) {
                FloatingActionButton(onClick = onAddDocumento) {
                    Icon(Icons.Default.Add, "Agregar documento")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Filtro estado
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val estadoOpciones = listOf(null to "Todos", "pendiente" to "Pendientes", "pagado" to "Pagados", "anulado" to "Anulados")
                items(estadoOpciones) { (valor, label) ->
                    FilterChip(
                        selected = uiState.filtroEstado == valor,
                        onClick = { viewModel.setFiltroEstado(valor) },
                        label = { Text(label, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            // Filtro días vencimiento
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val diasOpciones = listOf(null to "Todos", 7 to "7 días", 15 to "15 días", 30 to "30 días", 60 to "60 días")
                items(diasOpciones) { (valor, label) ->
                    FilterChip(
                        selected = uiState.filtroDias == valor,
                        onClick = { viewModel.setFiltroDias(valor) },
                        label = { Text(label, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.documentos.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay documentos", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.documentos, key = { it.id }) { doc ->
                        DocumentoCard(
                            documento = doc,
                            colorMonto = colorMonto,
                            isOwner = viewModel.isOwner(),
                            onMarcarPagado = { showMarcarPagadoDialog = doc },
                            onAnular = { viewModel.anular(doc.id) }
                        )
                    }
                }
            }
        }
    }

    // Dialog marcar pagado
    showMarcarPagadoDialog?.let { doc ->
        MarcarPagadoDialog(
            onConfirm = { fechaPago, numeroSeguimiento ->
                viewModel.marcarPagado(doc.id, fechaPago, numeroSeguimiento)
                showMarcarPagadoDialog = null
            },
            onDismiss = { showMarcarPagadoDialog = null }
        )
    }
}

@Composable
private fun DocumentoCard(
    documento: Documento,
    colorMonto: Color,
    isOwner: Boolean,
    onMarcarPagado: () -> Unit,
    onAnular: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("es", "CL"))
    val fechaVenc = try { LocalDate.parse(documento.fechaVencimiento).format(formatter) } catch (e: Exception) { documento.fechaVencimiento }

    val estadoColor = when (documento.estado) {
        "pagado" -> Color(0xFF2E7D32)
        "anulado" -> Color(0xFF757575)
        else -> Color(0xFFF57F17) // amber para pendiente
    }

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(documento.descripcion, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    documento.categoria?.let {
                        Text(it.replaceFirstChar { c -> c.uppercase() }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("Vence: $fechaVenc", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        formatMonto(documento.monto),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorMonto
                    )
                    Surface(color = estadoColor.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
                        Text(
                            documento.estado.replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = estadoColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Detalles expandibles + acciones
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    documento.numeroDocumento?.let { Text("Nº: $it", style = MaterialTheme.typography.bodySmall) }
                    documento.metodoPago?.let { Text("Método: ${it.replaceFirstChar { c -> c.uppercase() }}", style = MaterialTheme.typography.bodySmall) }
                    documento.notas?.let { Text("Notas: $it", style = MaterialTheme.typography.bodySmall) }

                    if (isOwner && documento.estado == "pendiente") {
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = onMarcarPagado, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Marcar Pagado")
                            }
                            OutlinedButton(onClick = onAnular, modifier = Modifier.weight(1f)) {
                                Text("Anular")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MarcarPagadoDialog(
    onConfirm: (fechaPago: String, numeroSeguimiento: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var fechaPago by remember { mutableStateOf(LocalDate.now().toString()) }
    var numeroSeguimiento by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Marcar como Pagado") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = fechaPago,
                    onValueChange = { fechaPago = it },
                    label = { Text("Fecha de pago (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = numeroSeguimiento,
                    onValueChange = { numeroSeguimiento = it },
                    label = { Text("Nº seguimiento (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(fechaPago, numeroSeguimiento.takeIf { it.isNotBlank() })
            }) { Text("Confirmar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

private fun formatMonto(monto: Long): String {
    val formatted = "%,d".format(monto).replace(",", ".")
    return "$$formatted"
}
