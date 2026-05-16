package com.nexo.empresas.presentation.contrataciones

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector

// ── Modelo local ─────────────────────────────────────────────────────────────
private data class ContratacionItem(
    val label: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContratacionesScreen(
    onBack: () -> Unit,
    onNavigateToSimulador: () -> Unit,
    onNavigateToFiniquito: () -> Unit,
    onNavigateToMultiTrabajador: () -> Unit
) {
    val items = listOf(
        ContratacionItem(
            label = "Simulador de Contratación",
            description = "Estima el costo mensual de contratar un trabajador",
            icon = Icons.Default.Calculate
        ),
        ContratacionItem(
            label = "Simulador de Finiquito",
            description = "Calcula el finiquito según el Código del Trabajo",
            icon = Icons.Default.Calculate
        ),
        ContratacionItem(
            label = "Multi-Trabajador",
            description = "Compara costos de contratación para varios trabajadores",
            icon = Icons.Default.Group
        )
    )

    val actions = listOf(
        onNavigateToSimulador,
        onNavigateToFiniquito,
        onNavigateToMultiTrabajador
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contrataciones") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Herramientas laborales",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            items.forEachIndexed { index, item ->
                Card(
                    onClick = { actions[index]() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.label,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                item.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
