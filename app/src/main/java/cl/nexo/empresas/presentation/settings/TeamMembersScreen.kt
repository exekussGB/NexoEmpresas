package cl.nexo.empresas.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import cl.nexo.empresas.core.tutorial.TutorialModule
import cl.nexo.empresas.presentation.tutorial.ModuleTutorialLauncher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamMembersScreen(
    onBack: () -> Unit,
    viewModel: TeamMembersViewModel = hiltViewModel()
) {
    val members by viewModel.members.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()
    val isOwner = viewModel.isOwner
    val snackbarHostState = remember { SnackbarHostState() }

    // Show role change dialog
    var showRoleDialog by remember { mutableStateOf<TeamMember?>(null) }
    var showDeleteDialog by remember { mutableStateOf<TeamMember?>(null) }

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // Role change dialog
    showRoleDialog?.let { member ->
        val roles = listOf("admin" to "Administrador", "viewer" to "Visualizador")
        AlertDialog(
            onDismissRequest = { showRoleDialog = null },
            title = { Text("Cambiar rol") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Selecciona el nuevo rol para ${member.email}:")
                    Spacer(modifier = Modifier.height(8.dp))
                    roles.forEach { (roleValue, roleLabel) ->
                        val selected = member.rol == roleValue
                        OutlinedCard(
                            onClick = {
                                if (!selected) {
                                    viewModel.updateRole(member.memberId, roleValue)
                                }
                                showRoleDialog = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (selected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = roleLabel,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = when (roleValue) {
                                        "admin" -> "Puede agregar, editar y eliminar datos"
                                        "viewer" -> "Solo puede ver la información"
                                        else -> ""
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoleDialog = null }) { Text("Cancelar") }
            }
        )
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { member ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Eliminar miembro") },
            text = { Text("¿Estás seguro de eliminar a ${member.email} del equipo? Perderá acceso a la empresa.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeMember(member.memberId)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFC62828))
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Miembros del Equipo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        ModuleTutorialLauncher(TutorialModule.EMPRESA_MIEMBROS)
        if (isLoading && members.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header card with role legend
                item {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Roles y permisos",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            HorizontalDivider()
                            RoleLegendItem("Dueño", "Control total. Puede eliminar la empresa y gestionar miembros.", Color(0xFF1565C0))
                            RoleLegendItem("Administrador", "Puede agregar, editar y eliminar documentos, contactos y cuentas.", Color(0xFF2E7D32))
                            RoleLegendItem("Visualizador", "Solo puede ver la información. No puede modificar nada.", Color(0xFF6A1B9A))
                        }
                    }
                }

                item {
                    Text(
                        text = "${members.size} miembro${if (members.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(members) { member ->
                    MemberCard(
                        member = member,
                        isOwner = isOwner,
                        onChangeRole = { showRoleDialog = member },
                        onRemove = { showDeleteDialog = member }
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleLegendItem(role: String, description: String, color: Color) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(10.dp)
                .background(color, CircleShape)
        )
        Column {
            Text(text = role, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MemberCard(
    member: TeamMember,
    isOwner: Boolean,
    onChangeRole: () -> Unit,
    onRemove: () -> Unit
) {
    val roleColor = when (member.rol) {
        "owner" -> Color(0xFF1565C0)
        "admin" -> Color(0xFF2E7D32)
        else -> Color(0xFF6A1B9A)
    }
    val roleLabel = when (member.rol) {
        "owner" -> "Dueño"
        "admin" -> "Administrador"
        else -> "Visualizador"
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier.size(44.dp).background(roleColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.email.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                    color = roleColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.email,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                SuggestionChip(
                    onClick = { if (isOwner && member.rol != "owner") onChangeRole() },
                    label = { Text(roleLabel, style = MaterialTheme.typography.labelSmall) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = roleColor.copy(alpha = 0.12f),
                        labelColor = roleColor
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Delete button (only owner can remove non-owners)
            if (isOwner && member.rol != "owner") {
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color(0xFFC62828)
                    )
                }
            }
        }
    }
}
