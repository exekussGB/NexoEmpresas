package com.nexo.empresas.dte.ui.dte

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexo.empresas.dte.data.model.TipoDte
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmitirDteScreen(
    empresaId: String,
    onNavigateBack: () -> Unit,
    onDteEmitido: (String) -> Unit,
    viewModel: DteViewModel = hiltViewModel()
) {
    val uiState by viewModel.emitirState.collectAsStateWithLifecycle()
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

    // Navegar al detalle cuando se emite exitosamente
    LaunchedEffect(uiState.success, uiState.dteEmitido) {
        if (uiState.success && uiState.dteEmitido?.id != null) {
            onDteEmitido(uiState.dteEmitido!!.id!!)
            viewModel.resetEmitirState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emitir Documento") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = paddingValues.calculateTopPadding() + 8.dp,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Tipo de DTE ────────────────────────────────────────────────
            item {
                SectionTitle("Tipo de Documento")
                TipoDteSelector(
                    tipoSeleccionado = uiState.tipoDte,
                    onTipoSelected = viewModel::onTipoDteChange
                )
            }

            // ── Datos del receptor ─────────────────────────────────────────
            item {
                SectionTitle("Receptor")
                ReceptorForm(
                    rut = uiState.rutReceptor,
                    razonSocial = uiState.razonSocialReceptor,
                    giro = uiState.giroReceptor,
                    direccion = uiState.direccionReceptor,
                    lookupLoading = uiState.rutLookupLoading,
                    lookupError = uiState.rutLookupError,
                    onRutChange = viewModel::onRutReceptorChange,
                    onRazonSocialChange = viewModel::onRazonSocialChange,
                    onGiroChange = viewModel::onGiroChange,
                    onDireccionChange = viewModel::onDireccionChange,
                    onLookupRut = { viewModel.lookupRut(uiState.rutReceptor) }
                )
            }

            // ── Items ──────────────────────────────────────────────────────
            item { SectionTitle("Items / Detalle") }

            itemsIndexed(uiState.items) { index, item ->
                ItemDteForm(
                    index = index,
                    item = item,
                    onItemChange = { viewModel.onItemChange(index, it) },
                    onEliminar = { viewModel.eliminarItem(index) },
                    canDelete = uiState.items.size > 1
                )
            }

            item {
                OutlinedButton(
                    onClick = viewModel::agregarItem,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Agregar ítem")
                }
            }

            // ── Resumen de totales ─────────────────────────────────────────
            item {
                val neto = uiState.items.sumOf { it.montoNeto }
                val iva = (neto * 0.19).toLong()
                val total = neto + iva

                ResumenTotales(
                    neto = formatter.format(neto),
                    iva = formatter.format(iva),
                    total = formatter.format(total)
                )
            }

            // ── Error y botón emitir ───────────────────────────────────────
            item {
                uiState.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Button(
                    onClick = { viewModel.emitirDte(empresaId) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Emitiendo...")
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Emitir y enviar al SII")
                    }
                }
            }
        }
    }
}

// ─── Selector tipo DTE ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TipoDteSelector(
    tipoSeleccionado: TipoDte,
    onTipoSelected: (TipoDte) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = tipoSeleccionado.descripcion,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tipo de documento") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TipoDte.entries.forEach { tipo ->
                DropdownMenuItem(
                    text = { Text("(${tipo.codigo}) ${tipo.descripcion}") },
                    onClick = {
                        onTipoSelected(tipo)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ─── Formulario receptor ──────────────────────────────────────────────────────

@Composable
private fun ReceptorForm(
    rut: String,
    razonSocial: String,
    giro: String,
    direccion: String,
    lookupLoading: Boolean,
    lookupError: String?,
    onRutChange: (String) -> Unit,
    onRazonSocialChange: (String) -> Unit,
    onGiroChange: (String) -> Unit,
    onDireccionChange: (String) -> Unit,
    onLookupRut: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = rut,
                onValueChange = onRutChange,
                label = { Text("RUT Receptor") },
                placeholder = { Text("12345678-9") },
                modifier = Modifier.weight(1f),
                isError = lookupError != null,
                supportingText = lookupError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = onLookupRut,
                enabled = rut.length >= 8 && !lookupLoading
            ) {
                if (lookupLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Search, contentDescription = "Buscar RUT en SII")
                }
            }
        }

        OutlinedTextField(
            value = razonSocial,
            onValueChange = onRazonSocialChange,
            label = { Text("Razón Social") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = giro,
            onValueChange = onGiroChange,
            label = { Text("Giro (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = direccion,
            onValueChange = onDireccionChange,
            label = { Text("Dirección (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

// ─── Formulario item ──────────────────────────────────────────────────────────

@Composable
private fun ItemDteForm(
    index: Int,
    item: ItemFormState,
    onItemChange: (ItemFormState) -> Unit,
    onEliminar: () -> Unit,
    canDelete: Boolean
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ítem ${index + 1}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                if (canDelete) {
                    IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar ítem", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            OutlinedTextField(
                value = item.descripcion,
                onValueChange = { onItemChange(item.copy(descripcion = it)) },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = item.cantidad,
                    onValueChange = { onItemChange(item.copy(cantidad = it)) },
                    label = { Text("Cantidad") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = item.precioUnitario,
                    onValueChange = { onItemChange(item.copy(precioUnitario = it)) },
                    label = { Text("Precio unitario") },
                    modifier = Modifier.weight(2f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = item.descuento,
                    onValueChange = { onItemChange(item.copy(descuento = it)) },
                    label = { Text("Desc. %") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }

            if (item.montoNeto > 0) {
                Text(
                    text = "Subtotal neto: ${formatter.format(item.montoNeto)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ─── Resumen totales ──────────────────────────────────────────────────────────

@Composable
private fun ResumenTotales(neto: String, iva: String, total: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Resumen", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            TotalRow("Monto neto", neto)
            TotalRow("IVA (19%)", iva)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            TotalRow("Total", total, bold = true)
        }
    }
}

@Composable
private fun TotalRow(label: String, value: String, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}
