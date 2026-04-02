package cl.nexo.empresas.presentation.cuentas

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cl.nexo.empresas.data.model.CuentaCorriente
import java.text.NumberFormat
import java.util.Locale
import cl.nexo.empresas.core.tutorial.TutorialModule
import cl.nexo.empresas.presentation.tutorial.ModuleTutorialLauncher

private val clpFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

private fun formatCLP(amount: Long): String = clpFormat.format(amount)

private fun tipoIcon(tipo: String): ImageVector = when (tipo) {
    "banco" -> Icons.Default.AccountBalance
    "caja"  -> Icons.Default.Money
    else    -> Icons.Default.Wallet
}

private fun tipoLabel(tipo: String): String = when (tipo) {
    "banco" -> "Banco"
    "caja"  -> "Caja"
    else    -> "Otro"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuentasCorrientesScreen(
    onBack: () -> Unit,
    viewModel: CuentasCorrientesViewModel = hiltViewModel()
) {
    val uiState    by viewModel.uiState.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val editTarget by viewModel.editTarget.collectAsState()
    val saveState  by viewModel.saveState.collectAsState()
    val filtroActiva by viewModel.filtroActiva.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadCuentas() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cuentas Corrientes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openCreateDialog() }) {
                Icon(Icons.Default.Add, "Nueva cuenta")
            }
        }
    ) { padding ->
        ModuleTutorialLauncher(TutorialModule.CUENTAS)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            FiltroTabs(filtroActiva = filtroActiva, onFiltroChange = viewModel::setFiltro)

            when (val state = uiState) {
                is CuentasUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is CuentasUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadCuentas() }) { Text("Reintentar") }
                        }
                    }
                }

                is CuentasUiState.Success -> {
                    if (state.cuentas.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.AccountBalance,
                                    null,
                                    modifier = Modifier.size(56.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Sin cuentas registradas",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    "Presiona + para agregar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.cuentas, key = { it.id }) { cuenta ->
                                CuentaCard(
                                    cuenta = cuenta,
                                    onEdit = { viewModel.openEditDialog(cuenta) },
                                    onToggleActiva = { viewModel.toggleActiva(cuenta) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        CuentaDialog(
            editTarget = editTarget,
            saveState  = saveState,
            onDismiss  = { viewModel.closeDialog() },
            onSave     = { nombre, tipo, numeroCuenta, saldoInicial, activa ->
                viewModel.save(nombre, tipo, numeroCuenta, saldoInicial, activa)
            }
        )
    }
}

@Composable
private fun FiltroTabs(
    filtroActiva: Boolean?,
    onFiltroChange: (Boolean?) -> Unit
) {
    val options = listOf("Todas" to null, "Activas" to true, "Inactivas" to false)
    TabRow(
        selectedTabIndex = options.indexOfFirst { it.second == filtroActiva }.coerceAtLeast(0)
    ) {
        options.forEachIndexed { index, (label, value) ->
            Tab(
                selected = filtroActiva == value,
                onClick  = { onFiltroChange(value) },
                text     = { Text(label) }
            )
        }
    }
}

@Composable
private fun CuentaCard(
    cuenta: CuentaCorriente,
    onEdit: () -> Unit,
    onToggleActiva: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (cuenta.activa)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = tipoIcon(cuenta.tipo),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (cuenta.activa)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text  = cuenta.nombre,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.width(8.dp))
                    AssistChip(
                        onClick = {},
                        label   = { Text(tipoLabel(cuenta.tipo), style = MaterialTheme.typography.labelSmall) }
                    )
                }
                if (!cuenta.numeroCuenta.isNullOrBlank()) {
                    Text(
                        text  = "N° ${cuenta.numeroCuenta}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text  = "Saldo inicial: ${formatCLP(cuenta.saldoInicial)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (!cuenta.activa) {
                    Text(
                        text  = "Inactiva",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary)
                }
                Switch(
                    checked         = cuenta.activa,
                    onCheckedChange = { onToggleActiva() },
                    modifier        = Modifier.size(36.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CuentaDialog(
    editTarget: CuentaCorriente?,
    saveState: SaveState,
    onDismiss: () -> Unit,
    onSave: (nombre: String, tipo: String, numeroCuenta: String?, saldoInicial: Long, activa: Boolean) -> Unit
) {
    val isEditing = editTarget != null

    var nombre       by remember(editTarget) { mutableStateOf(editTarget?.nombre ?: "") }
    var tipo         by remember(editTarget) { mutableStateOf(editTarget?.tipo ?: "banco") }
    var numeroCuenta by remember(editTarget) { mutableStateOf(editTarget?.numeroCuenta ?: "") }
    var saldoTexto   by remember(editTarget) { mutableStateOf(editTarget?.saldoInicial?.toString() ?: "0") }
    var activa       by remember(editTarget) { mutableStateOf(editTarget?.activa ?: true) }

    var nombreError by remember { mutableStateOf(false) }
    var saldoError  by remember { mutableStateOf(false) }

    val tipos = listOf("banco" to "Banco", "caja" to "Caja", "otro" to "Otro")

    AlertDialog(
        onDismissRequest = { if (saveState !is SaveState.Loading) onDismiss() },
        title = { Text(if (isEditing) "Editar cuenta" else "Nueva cuenta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Nombre
                OutlinedTextField(
                    value         = nombre,
                    onValueChange = { nombre = it; nombreError = false },
                    label         = { Text("Nombre *") },
                    isError       = nombreError,
                    supportingText = if (nombreError) {{ Text("Campo obligatorio") }} else null,
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true
                )

                // Tipo
                Text("Tipo", style = MaterialTheme.typography.labelMedium)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    tipos.forEachIndexed { index, (value, label) ->
                        SegmentedButton(
                            selected = tipo == value,
                            onClick  = { tipo = value },
                            shape    = SegmentedButtonDefaults.itemShape(index, tipos.size),
                            label    = { Text(label) }
                        )
                    }
                }

                // Número de cuenta — solo dígitos
                OutlinedTextField(
                    value         = numeroCuenta,
                    onValueChange = { newVal ->
                        if (newVal.all { it.isDigit() }) numeroCuenta = newVal
                    },
                    label         = { Text("N° de cuenta (opcional)") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Saldo inicial — solo dígitos (sin signo)
                OutlinedTextField(
                    value         = saldoTexto,
                    onValueChange = { newVal ->
                        if (newVal.all { it.isDigit() }) {
                            saldoTexto = newVal
                            saldoError = false
                        }
                    },
                    label         = { Text("Saldo inicial (CLP)") },
                    isError       = saldoError,
                    supportingText = if (saldoError) {{ Text("Ingresa un monto válido") }} else null,
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix        = { Text("$") }
                )

                // Activa toggle (solo en edición)
                if (isEditing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cuenta activa")
                        Switch(checked = activa, onCheckedChange = { activa = it })
                    }
                }

                if (saveState is SaveState.Error) {
                    Text(
                        text  = saveState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    nombreError = nombre.isBlank()
                    val saldo = saldoTexto.trim().toLongOrNull()
                    saldoError = saldo == null
                    if (!nombreError && !saldoError) {
                        onSave(
                            nombre,
                            tipo,
                            numeroCuenta.ifBlank { null },
                            saldo!!,
                            activa
                        )
                    }
                },
                enabled = saveState !is SaveState.Loading
            ) {
                if (saveState is SaveState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (isEditing) "Guardar" else "Crear")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick  = onDismiss,
                enabled  = saveState !is SaveState.Loading
            ) { Text("Cancelar") }
        }
    )
}
