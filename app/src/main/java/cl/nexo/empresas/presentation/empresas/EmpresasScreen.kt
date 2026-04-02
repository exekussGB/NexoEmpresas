package cl.nexo.empresas.presentation.empresas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmpresasScreen(
    onEmpresaSelected: () -> Unit,
    vm: EmpresasViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val showDialog by vm.showCreateDialog.collectAsState()
    val createState by vm.createState.collectAsState()

    LaunchedEffect(Unit) { vm.loadEmpresas() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis Empresas") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { vm.showCreateDialog(true) }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar empresa")
            }
        }
    ) { padding ->
        when (val s = state) {
            is EmpresasUiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is EmpresasUiState.Success -> {
                if (s.empresas.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Business, contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.height(16.dp))
                            Text("Aún no tienes empresas", style = MaterialTheme.typography.titleMedium)
                            Text("Toca + para crear la primera", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline)
                        }
                    }
                } else {
                    LazyColumn(contentPadding = padding) {
                        items(s.empresas) { empresa ->
                            ListItem(
                                headlineContent = { Text(empresa.nombre) },
                                supportingContent = { Text("RUT: ${empresa.rut}") },
                                leadingContent = { Icon(Icons.Default.Business, null) },
                                modifier = Modifier.clickable {
                                    vm.selectEmpresa(empresa)
                                    onEmpresaSelected()
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }

            is EmpresasUiState.Error -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { Text(s.message, color = MaterialTheme.colorScheme.error) }

            else -> {}
        }
    }

    if (showDialog) {
        CrearEmpresaDialog(
            createState = createState,
            onConfirm = { nombre, rut, giro -> vm.createEmpresa(nombre, rut, giro) },
            onDismiss = { vm.showCreateDialog(false) }
        )
    }
}

@Composable
fun CrearEmpresaDialog(
    createState: CreateEmpresaState,
    onConfirm: (nombre: String, rut: String, giro: String) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var rut by remember { mutableStateOf("") }
    var giro by remember { mutableStateOf("") }
    var rutTouched by remember { mutableStateOf(false) }
    val isLoading = createState is CreateEmpresaState.Loading

    val cleanRut = rut.replace(".", "").replace("-", "")
    val isRutValid = if (cleanRut.isEmpty()) false else isValidRut(cleanRut)
    val showRutError = rutTouched && rut.isNotBlank() && !isRutValid

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Nueva Empresa") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { newValue ->
                        nombre = newValue.filter { c ->
                            c.isLetter() || c.isWhitespace() || c.isDigit() ||
                                c == '.' || c == ',' || c == '-' || c == '&' ||
                                c == 'á' || c == 'é' || c == 'í' || c == 'ó' || c == 'ú' ||
                                c == 'Á' || c == 'É' || c == 'Í' || c == 'Ó' || c == 'Ú' ||
                                c == 'ñ' || c == 'Ñ'
                        }
                    },
                    label = { Text("Nombre *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = rut,
                    onValueChange = { newValue ->
                        rutTouched = true
                        rut = formatRut(newValue)
                    },
                    label = { Text("RUT *") },
                    placeholder = { Text("76.123.456-7") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = showRutError,
                    supportingText = if (showRutError) {
                        { Text("RUT no válido") }
                    } else null
                )
                OutlinedTextField(
                    value = giro,
                    onValueChange = { giro = it },
                    label = { Text("Giro (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                if (createState is CreateEmpresaState.Error) {
                    Text(
                        text = createState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(nombre, rut, giro) },
                enabled = nombre.isNotBlank() && rut.isNotBlank() && isRutValid && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Crear")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text("Cancelar") }
        }
    )
}

private fun formatRut(input: String): String {
    val clean = input.filter { it.isDigit() || it.equals('k', ignoreCase = true) }
    if (clean.isEmpty()) return ""

    val body = if (clean.length > 1) clean.dropLast(1) else ""
    val dv = if (clean.length > 1) clean.last().uppercase() else clean.uppercase()

    if (body.isEmpty()) return dv

    val formatted = buildString {
        body.reversed().forEachIndexed { index, c ->
            if (index > 0 && index % 3 == 0) append('.')
            append(c)
        }
    }.reversed()

    return "$formatted-$dv"
}

private fun isValidRut(input: String): Boolean {
    val clean = input.replace(".", "").replace("-", "").uppercase()
    if (clean.length < 2) return false

    val body = clean.dropLast(1)
    val dv = clean.last()

    if (!body.all { it.isDigit() }) return false

    var sum = 0
    var multiplier = 2
    for (c in body.reversed()) {
        sum += c.digitToInt() * multiplier
        multiplier = if (multiplier == 7) 2 else multiplier + 1
    }

    val remainder = 11 - (sum % 11)
    val expectedDv = when (remainder) {
        11 -> '0'
        10 -> 'K'
        else -> ('0' + remainder)
    }

    return dv == expectedDv
}
