package cl.nexo.empresas.presentation.alertas

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current

    // ── Permiso POST_NOTIFICATIONS (Android 13+) ──────────────────────────────
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* resultado manejado silenciosamente; el sistema muestra el diálogo */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    // ─────────────────────────────────────────────────────────────────────────

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

                    // Banner informativo si no hay permiso
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚠️ Permite notificaciones en ajustes del sistema para recibir alertas.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
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
