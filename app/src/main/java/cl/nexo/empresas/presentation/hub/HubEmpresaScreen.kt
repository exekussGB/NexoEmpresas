package cl.nexo.empresas.presentation.hub

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.nexo.empresas.presentation.navigation.Screen
import androidx.compose.foundation.lazy.grid.GridItemSpan

data class HubItem(val label: String, val icon: ImageVector, val screen: Screen)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubEmpresaScreen(
    onNavigate: (Screen) -> Unit,
    onLogout: () -> Unit
) {
    val items = listOf(
        HubItem("Resumen",       Icons.Default.Dashboard,      Screen.Dashboard),
        HubItem("Ingresar Doc.", Icons.Default.AddCircle,       Screen.AddDocumento),
        HubItem("Por Pagar",     Icons.Default.TrendingDown,    Screen.CuentasPagar),
        HubItem("Por Cobrar",    Icons.Default.TrendingUp,      Screen.CuentasCobrar),
        HubItem("Cheques",       Icons.Default.Receipt,         Screen.Cheques),
        HubItem("Cuentas",       Icons.Default.AccountBalance,  Screen.Cuentas),
        HubItem("Contactos",     Icons.Default.Contacts,        Screen.Contactos),
        HubItem("Simulador",     Icons.Default.Calculate,       Screen.Simulador),
        HubItem("Opciones",      Icons.Default.Settings,        Screen.Opciones),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NexoEmpresas") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items.dropLast(1)) { item ->
                Card(
                    onClick = { onNavigate(item.screen) },
                    modifier = Modifier.aspectRatio(1f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(item.icon, null, modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Text(item.label, style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center)
                    }
                }
            }

            // Opciones a todo el ancho
            item(span = { GridItemSpan(2) }) {
                val opcion = items.last()
                Card(
                    onClick = { onNavigate(opcion.screen) },
                    modifier = Modifier.fillMaxWidth().height(80.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(opcion.icon, null, modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(opcion.label, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}