package cl.nexo.empresas.presentation.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onAlertas: () -> Unit,
    onTutoriales: () -> Unit = {},
    onTeamMembers: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val empresa by viewModel.empresa.collectAsState()
    val inviteCode by viewModel.inviteCode.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val userEmail = viewModel.userEmail
    val userRole = viewModel.userRole
    val context = LocalContext.current

    // Estados de invitación
    val invitaciones by viewModel.invitaciones.collectAsState()
    val inviteState by viewModel.inviteState.collectAsState()

    // Estado local para el campo de email
    var emailInput by remember { mutableStateOf("") }

    // Diálogos de confirmación
    var showInviteConfirmDialog by remember { mutableStateOf(false) }
    var showAcceptDialog by remember { mutableStateOf<cl.nexo.empresas.data.model.InvitacionPendiente?>(null) }
    var showRejectDialog by remember { mutableStateOf<cl.nexo.empresas.data.model.InvitacionPendiente?>(null) }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar feedback de invitación
    LaunchedEffect(inviteState) {
        when (val state = inviteState) {
            is InviteState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                emailInput = ""
                viewModel.resetInviteState()
            }
            is InviteState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetInviteState()
            }
            else -> {}
        }
    }

    // Diálogo de confirmación de invitación por email
    if (showInviteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showInviteConfirmDialog = false },
            title = { Text("Confirmar invitación") },
            text = { Text("¿Estás seguro de invitar a $emailInput?") },
            confirmButton = {
                TextButton(onClick = {
                    showInviteConfirmDialog = false
                    viewModel.invitarUsuario(emailInput)
                }) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInviteConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de confirmación de aceptar invitación
    showAcceptDialog?.let { inv ->
        AlertDialog(
            onDismissRequest = { showAcceptDialog = null },
            title = { Text("Confirmar acceso") },
            text = { Text("¿Confirmar acceso para ${inv.emailInvitado}?") },
            confirmButton = {
                TextButton(onClick = {
                    showAcceptDialog = null
                    viewModel.aceptarInvitacion(inv)
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAcceptDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de confirmación de rechazar invitación
    showRejectDialog?.let { inv ->
        AlertDialog(
            onDismissRequest = { showRejectDialog = null },
            title = { Text("Rechazar invitación") },
            text = { Text("¿Rechazar invitación de ${inv.emailInvitado}?") },
            confirmButton = {
                TextButton(onClick = {
                    showRejectDialog = null
                    viewModel.rechazarInvitacion(inv)
                }) {
                    Text("Rechazar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Opciones") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Header: avatar circular con inicial + email ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userEmail.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                    Column {
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (userRole == "owner") "Propietario" else "Visualizador",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // --- Card Empresa ---
                empresa?.let { emp ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = emp.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "RUT: ${emp.rut}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            emp.giro?.let { giro ->
                                Text(
                                    text = "Giro: $giro",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Chip de rol
                            SuggestionChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = if (userRole == "owner") "Propietario" else "Visualizador",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (userRole == "owner")
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        }
                    }
                }

                // --- Card "Invitar Usuario por Email" (solo owner) ---
                if (userRole == "owner") {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Invitar Usuario",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Ingresa el correo electrónico del usuario que deseas invitar.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Correo electrónico") },
                                placeholder = { Text("ejemplo@correo.com") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = { showInviteConfirmDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = emailInput.isNotBlank() && inviteState !is InviteState.Loading
                            ) {
                                if (inviteState is InviteState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Enviar invitación")
                            }
                        }
                    }

                    // --- Invitaciones Pendientes (solo owner) ---
                    if (invitaciones.isNotEmpty()) {
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Invitaciones Pendientes",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                HorizontalDivider()
                                invitaciones.forEach { inv ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = inv.emailInvitado,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "Pendiente",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = { showAcceptDialog = inv }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Aceptar",
                                                    tint = Color(0xFF2E7D32)
                                                )
                                            }
                                            IconButton(
                                                onClick = { showRejectDialog = inv }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Rechazar",
                                                    tint = Color(0xFFC62828)
                                                )
                                            }
                                        }
                                    }
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }

                // --- Card "Invitar miembro" por código (solo owner, secundario) ---
                if (userRole == "owner" && inviteCode.isNotBlank()) {
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Código de invitación",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "También puedes compartir este código para que otros se unan:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = inviteCode,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Código de invitación", inviteCode)
                                    clipboard.setPrimaryClip(clip)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Copiar código")
                            }
                        }
                    }
                }

                // --- ListTile "Miembros del Equipo" ---
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onTeamMembers
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "👥 Miembros del Equipo",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // --- ListTile "Configurar Alertas" ---
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onAlertas
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🔔 Configurar Alertas",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // --- ListTile "Tutoriales y Ayuda" ---
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onTutoriales
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "📚 Tutoriales y Ayuda",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // --- Botón Cerrar Sesión ---
                OutlinedButton(
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFC62828)
                    )
                ) {
                    Text("Cerrar Sesión")
                }
            }
        }
    }
}
