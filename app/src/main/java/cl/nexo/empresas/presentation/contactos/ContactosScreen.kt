package cl.nexo.empresas.presentation.contactos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import cl.nexo.empresas.data.model.Contacto
import cl.nexo.empresas.core.tutorial.TutorialModule
import cl.nexo.empresas.presentation.tutorial.ModuleTutorialLauncher

// ── RUT auto-formato chileno (ej: 12345678-9 → 12.345.678-9) ─────────────────
private fun formatRut(input: String): String {
    // Conserva solo dígitos y K/k, convertir a mayúscula
    val clean = input.filter { it.isDigit() || it.lowercaseChar() == 'k' }.uppercase()
    if (clean.length <= 1) return clean

    val verifier = clean.last()
    val body = clean.dropLast(1)

    // Inserta puntos cada 3 dígitos desde la derecha
    val sb = StringBuilder()
    body.reversed().forEachIndexed { i, c ->
        if (i > 0 && i % 3 == 0) sb.insert(0, '.')
        sb.insert(0, c)
    }
    return "$sb-$verifier"
}

private fun isValidRut(rut: String): Boolean {
    if (rut.isBlank()) return true // campo opcional → vacío es válido
    val clean = rut.filter { it.isDigit() || it.lowercaseChar() == 'k' }.uppercase()
    if (clean.length < 2) return false
    val body = clean.dropLast(1)
    val dv = clean.last()
    if (!body.all { it.isDigit() }) return false

    // Validación módulo 11
    var sum = 0
    var mul = 2
    for (c in body.reversed()) {
        sum += c.digitToInt() * mul
        mul = if (mul == 7) 2 else mul + 1
    }
    val expected = when (val rem = 11 - (sum % 11)) {
        11 -> '0'
        10 -> 'K'
        else -> '0' + rem
    }
    return dv == expected
}

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
        ModuleTutorialLauncher(TutorialModule.CONTACTOS)
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
            Icon(
                imageVector = when (contacto.tipo) {
                    "proveedor" -> Icons.Default.LocalShipping
                    "cliente"   -> Icons.Default.Person
                    else        -> Icons.Default.People
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
                val (tipoLabel, tipoColor) = when (contacto.tipo) {
                    "proveedor" -> "Proveedor" to MaterialTheme.colorScheme.tertiary
                    "cliente"   -> "Cliente" to MaterialTheme.colorScheme.secondary
                    else        -> "Ambos" to MaterialTheme.colorScheme.primary
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
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary)
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
    var rut    by remember { mutableStateOf(contacto?.rut ?: "") }
    var tipo   by remember { mutableStateOf(contacto?.tipo ?: "ambos") }

    var nombreError by remember { mutableStateOf(false) }
    var rutError    by remember { mutableStateOf(false) }

    val isEditing = contacto != null
    val isLoading = saveState is SaveContactoState.Loading
    val errorMsg  = (saveState as? SaveContactoState.Error)?.message

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text(if (isEditing) "Editar Contacto" else "Nuevo Contacto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; nombreError = false },
                    label = { Text("Nombre *") },
                    isError = nombreError,
                    supportingText = if (nombreError) {{ Text("El nombre es obligatorio") }} else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = rut,
                    onValueChange = { input ->
                        rut = formatRut(input)
                        rutError = false
                    },
                    label = { Text("RUT (opcional)") },
                    placeholder = { Text("12.345.678-9") },
                    isError = rutError,
                    supportingText = if (rutError) {
                        { Text("RUT inválido. Ej: 12.345.678-9") }
                    } else {
                        { Text("Se formatea automáticamente", style = MaterialTheme.typography.labelSmall) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text("Tipo", style = MaterialTheme.typography.labelMedium)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf(
                        "proveedor" to "Proveedor",
                        "cliente"   to "Cliente",
                        "ambos"     to "Ambos"
                    ).forEachIndexed { index, (value, label) ->
                        SegmentedButton(
                            selected = tipo == value,
                            onClick  = { tipo = value },
                            shape    = SegmentedButtonDefaults.itemShape(index = index, count = 3),
                            label    = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            enabled  = !isLoading
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
                onClick = {
                    nombreError = nombre.isBlank()
                    rutError    = rut.isNotBlank() && !isValidRut(rut)
                    if (!nombreError && !rutError) onSave(nombre, rut, tipo)
                },
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
