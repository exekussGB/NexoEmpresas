package cl.nexo.empresas.presentation.alertas

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertasConfigScreen(
    onBack: () -> Unit,
    viewModel: AlertasViewModel = hiltViewModel()
) {
    val config by viewModel.config.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val error by viewModel.error.collectAsState()

    // Local state for form fields
    var dias by remember(config) { mutableIntStateOf(config?.diasAnticipacion ?: 5) }
    var cobros by remember(config) { mutableStateOf(config?.alertasCobros ?: true) }
    var pagos by remember(config) { mutableStateOf(config?.alertasPagos ?: true) }
    var cheques by remember(config) { mutableStateOf(config?.alertasCheques ?: true) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isSaved) {
        if (isSaved) {
            scope.launch {
                snackbarHostState.showSnackbar("¡Guardado!")
                viewModel.clearSaved()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurar Alertas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // --- Slider días de anticipación ---
                    Column {
                        Text(
                            text = "Días de anticipación: $dias",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Slider(
                            value = dias.toFloat(),
                            onValueChange = { dias = it.toInt() },
                            valueRange = 1f..15f,
                            steps = 13,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("1 día", style = MaterialTheme.typography.labelSmall)
                            Text("15 días", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    HorizontalDivider()

                    // --- Switches ---
                    SwitchRow(
                        label = "Alertas de cobros",
                        checked = cobros,
                        onCheckedChange = { cobros = it }
                    )
                    SwitchRow(
                        label = "Alertas de pagos",
                        checked = pagos,
                        onCheckedChange = { pagos = it }
                    )
                    SwitchRow(
                        label = "Alertas de cheques",
                        checked = cheques,
                        onCheckedChange = { cheques = it }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // --- Botón Guardar ---
                    Button(
                        onClick = {
                            viewModel.saveConfig(
                                dias = dias,
                                cobros = cobros,
                                pagos = pagos,
                                cheques = cheques
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
