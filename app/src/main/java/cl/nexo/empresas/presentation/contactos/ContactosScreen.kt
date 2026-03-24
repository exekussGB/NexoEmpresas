package cl.nexo.empresas.presentation.contactos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cl.nexo.empresas.data.model.Contacto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactosScreen(
    onBack: () -> Unit,
    viewModel: ContactosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filtro by viewModel.filtro.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val editando by viewModel.editando.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadContactos() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contactos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showDialog() }) {
                Icon(Icons.Default.Add, "Agregar contacto")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filtros
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroContacto.entries.forEach { f ->
                    FilterChip(
                        selected = filtro == f,
                        onClick = { viewModel.setFiltro(f) },
                        label = { Text(f.label) }
                    )
                }
            }

            when (val state = uiState) {
                is ContactosUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ContactosUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ErrorOutline, null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(8.dp))
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadContactos() }) { Text("Reintentar") }
                        }
                    }
                }
                is ContactosUiState.Success -> {
                    if (state.contactos.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.PeopleOutline, null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(12.dp))
                                Text("No hay contactos aún",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(4.dp))
                                Text("Toca + para agregar uno",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.contactos, key = { it.id }) { contacto ->
                                ContactoCard(
                                    contacto = contacto,
                                    onEdit = { viewModel.showDialog(contacto) },
                                    onToggleActivo = { viewModel.toggleActivo(contacto) }
                                )
                            }
                        }
                    }
                }
                else -> Unit
            }
        }
    }

    // Dialog Agregar / Editar
    if (showDialog) {
        ContactoDialog(
            contacto = editando,
            saveState = saveState,
            onDismiss = { viewModel.hideDialog() },
            onSave = { nombre, rut, tipo ->
                viewModel.saveContacto(nombre, rut, tipo)
            }
        )
    }
}

@Composable
private fun ContactoCard(
    contacto: Contacto,
    onEdit: () -> Unit,
    onToggleActivo: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (contacto.activo)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono según tipo
            Icon(
                imageVector = when (contacto.tipo) {
                    "proveedor" -> Icons.Default.LocalShipping
                    "cliente" -> Icons.Default.Person
                    else -> Icons.Default.People
                },
                contentDescription = null,
                tint = if (contacto.activo)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contacto.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (contacto.activo)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!contacto.rut.isNullOrBlank()) {
                    Text(
                        text = "RUT: ${contacto.rut}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(4.dp))
                // Badge tipo
                val (tipoLabel, tipoColor) = when (contacto.tipo) {
                    "proveedor" -> "Proveedor" to MaterialTheme.colorScheme.tertiary
                    "cliente" -> "Cliente" to MaterialTheme.colorScheme.secondary
                    else -> "Ambos" to MaterialTheme.colorScheme.primary
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = tipoColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = tipoLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = tipoColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            // Acciones
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Editar",
                    tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onToggleActivo) {
                Icon(
                    imageVector = if (contacto.activo) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                    contentDescription = if (contacto.activo) "Desactivar" else "Activar",
                    tint = if (contacto.activo) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactoDialog(
    contacto: Contacto?,
    saveState: SaveContactoState,
    onDismiss: () -> Unit,
    onSave: (nombre: String, rut: String, tipo: String) -> Unit
) {
    var nombre by remember { mutableStateOf(contacto?.nombre ?: "") }
    var rut by remember { mutableStateOf(contacto?.rut ?: "") }
    var tipo by remember { mutableStateOf(contacto?.tipo ?: "ambos") }

    val isEditing = contacto != null
    val isLoading = saveState is SaveContactoState.Loading
    val errorMsg = (saveState as? SaveContactoState.Error)?.message

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text(if (isEditing) "Editar Contacto" else "Nuevo Contacto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = rut,
                    onValueChange = { rut = it },
                    label = { Text("RUT (opcional)") },
                    placeholder = { Text("76.123.456-7") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )
                Text("Tipo", style = MaterialTheme.typography.labelMedium)
                // Segmented buttons para tipo
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf(
                        "proveedor" to "Proveedor",
                        "cliente" to "Cliente",
                        "ambos" to "Ambos"
                    ).forEachIndexed { index, (value, label) ->
                        SegmentedButton(
                            selected = tipo == value,
                            onClick = { tipo = value },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 3),
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            enabled = !isLoading
                        )
                    }
                }
                if (errorMsg != null) {
                    Text(errorMsg, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (nombre.isNotBlank()) onSave(nombre, rut, tipo) },
                enabled = nombre.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancelar")
            }
        }
    )
}
