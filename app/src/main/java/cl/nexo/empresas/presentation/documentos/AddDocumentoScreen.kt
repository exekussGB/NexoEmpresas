package cl.nexo.empresas.presentation.documentos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cl.nexo.empresas.data.model.CategoriaDocumento
import cl.nexo.empresas.data.model.MetodoPago
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentoScreen(
    onBack: () -> Unit,
    viewModel: AddDocumentoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.savedSuccessfully) {
        if (state.savedSuccessfully) onBack()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ingresar Documento") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Toggle Ingreso/Egreso
                item {
                    SegmentedTipoSelector(
                        selected = state.tipo,
                        onSelect = viewModel::setTipo
                    )
                }

                // Número de factura
                item {
                    OutlinedTextField(
                        value = state.numeroDocumento,
                        onValueChange = viewModel::setNumeroDocumento,
                        label = { Text("Número de factura (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Contacto
                item {
                    val contactoNombre = state.contactos.find { it.id == state.contactoId }?.nombre ?: "Sin contacto"
                    DropdownSelector(
                        label = "Contacto (opcional)",
                        selected = contactoNombre,
                        options = listOf("Sin contacto") + state.contactos.map { it.nombre },
                        onSelect = { nombre ->
                            val c = state.contactos.find { it.nombre == nombre }
                            viewModel.setContactoId(c?.id)
                        }
                    )
                }

                // Categoría
                item {
                    DropdownSelector(
                        label = "Categoría",
                        selected = CategoriaDocumento.entries.find { it.value == state.categoria }?.label ?: state.categoria,
                        options = CategoriaDocumento.entries.map { it.label },
                        onSelect = { label ->
                            val cat = CategoriaDocumento.entries.find { it.label == label }
                            viewModel.setCategoria(cat?.value ?: label)
                        }
                    )
                }

                // Descripción
                item {
                    OutlinedTextField(
                        value = state.descripcion,
                        onValueChange = viewModel::setDescripcion,
                        label = { Text("Descripción *") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )
                }

                // Monto
                item {
                    OutlinedTextField(
                        value = state.monto,
                        onValueChange = viewModel::setMonto,
                        label = { Text("Monto (CLP) *") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        prefix = { Text("\$") }
                    )
                }

                // Cuenta corriente
                item {
                    val cuentaNombre = state.cuentas.find { it.id == state.cuentaCorrienteId }?.nombre ?: "Sin cuenta"
                    DropdownSelector(
                        label = "Cuenta Corriente (opcional)",
                        selected = cuentaNombre,
                        options = listOf("Sin cuenta") + state.cuentas.map { it.nombre },
                        onSelect = { nombre ->
                            val c = state.cuentas.find { it.nombre == nombre }
                            viewModel.setCuentaId(c?.id)
                        }
                    )
                }

                // ── Documento de referencia ────────────────────────────────────────────
                // Muestra documentos pendientes del mismo tipo (cobros o pagos) con su fecha
                item {
                    val pendientes = state.pendientesFiltrados
                    if (pendientes.isNotEmpty()) {
                        val fmt = DateTimeFormatter.ofPattern("dd/MM/yy", Locale("es", "CL"))
                        fun fmtDoc(d: cl.nexo.empresas.data.model.Documento): String {
                            val fStr = try { LocalDate.parse(d.fechaVencimiento).format(fmt) } catch (e: Exception) { d.fechaVencimiento }
                            return "${d.descripcion} · Vence $fStr · ${formatMonto(d.monto)}"
                        }
                        val noneLabel = "Sin referencia"
                        val docLabels = pendientes.map { fmtDoc(it) }
                        val allOptions = listOf(noneLabel) + docLabels
                        val selectedLabel = state.referenciaDocId
                            ?.let { id -> pendientes.find { it.id == id }?.let { fmtDoc(it) } }
                            ?: noneLabel
                        val sectionLabel = if (state.tipo == "ingreso") "Cobro relacionado" else "Pago relacionado"
                        DropdownSelector(
                            label = "$sectionLabel (opcional)",
                            selected = selectedLabel,
                            options = allOptions,
                            onSelect = { sel ->
                                if (sel == noneLabel) {
                                    viewModel.setReferenciaDoc(null)
                                } else {
                                    val idx = docLabels.indexOf(sel)
                                    viewModel.setReferenciaDoc(pendientes.getOrNull(idx))
                                }
                            }
                        )
                    }
                }

                // Fechas
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.fechaMovimiento,
                            onValueChange = viewModel::setFechaMovimiento,
                            label = { Text("Fecha movimiento") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state.fechaVencimiento,
                            onValueChange = viewModel::setFechaVencimiento,
                            label = { Text("Fecha vencimiento") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                // Método de pago
                item {
                    DropdownSelector(
                        label = "Método de pago",
                        selected = MetodoPago.entries.find { it.value == state.metodoPago }?.label ?: state.metodoPago,
                        options = MetodoPago.entries.map { it.label },
                        onSelect = { label ->
                            val mp = MetodoPago.entries.find { it.label == label }
                            viewModel.setMetodoPago(mp?.value ?: label)
                        }
                    )
                }

                // Notas
                item {
                    OutlinedTextField(
                        value = state.notas,
                        onValueChange = viewModel::setNotas,
                        label = { Text("Notas (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }

                // Sección Cheques
                if (state.isChequePago) {
                    item {
                        Text("Cheques", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }

                    itemsIndexed(state.cheques) { index, cheque ->
                        ChequeFormCard(
                            cheque = cheque,
                            index = index,
                            onUpdate = { viewModel.updateCheque(index, it) },
                            onRemove = { viewModel.removeCheque(index) },
                            canRemove = state.cheques.size > 1
                        )
                    }

                    item {
                        val diff = state.chequesDiff
                        val diffColor = if (diff == 0L) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = viewModel::addCheque) {
                                Icon(Icons.Default.Add, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Agregar Cheque")
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Suma cheques: ${formatMonto(state.sumaChequesLong)}", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    if (diff == 0L) "✓ Cuadra" else "Diferencia: ${formatMonto(diff)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = diffColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Botón guardar
                item {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = viewModel::guardar,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !state.isSaving
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Guardar Documento", style = MaterialTheme.typography.titleSmall)
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun SegmentedTipoSelector(selected: String, onSelect: (String) -> Unit) {
    val ingresoSelected = selected == "ingreso"
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = { onSelect("ingreso") },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (ingresoSelected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.surfaceVariant,
                contentColor   = if (ingresoSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) { Text("💰 Ingreso", fontWeight = FontWeight.SemiBold) }
        Button(
            onClick = { onSelect("egreso") },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (!ingresoSelected) Color(0xFFC62828) else MaterialTheme.colorScheme.surfaceVariant,
                contentColor   = if (!ingresoSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) { Text("💳 Egreso", fontWeight = FontWeight.SemiBold) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelector(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelect(option); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun ChequeFormCard(
    cheque: ChequeForm,
    index: Int,
    onUpdate: (ChequeForm) -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Cheque ${index + 1}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                if (canRemove) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = cheque.numeroCheque,
                    onValueChange = { onUpdate(cheque.copy(numeroCheque = it)) },
                    label = { Text("Nº Cheque") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = cheque.banco,
                    onValueChange = { onUpdate(cheque.copy(banco = it)) },
                    label = { Text("Banco") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = cheque.monto,
                    onValueChange = { onUpdate(cheque.copy(monto = it)) },
                    label = { Text("Monto") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    prefix = { Text("\$") }
                )
                OutlinedTextField(
                    value = cheque.fechaCobro,
                    onValueChange = { onUpdate(cheque.copy(fechaCobro = it)) },
                    label = { Text("Fecha cobro") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }
    }
}

private fun formatMonto(monto: Long): String {
    val formatted = "%,d".format(monto).replace(",", ".")
    return "\$$formatted"
}
