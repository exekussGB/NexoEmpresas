package cl.nexo.empresas.presentation.hub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.nexo.empresas.core.tutorial.TutorialManager
import cl.nexo.empresas.core.tutorial.TutorialModule
import cl.nexo.empresas.presentation.navigation.Screen
import cl.nexo.empresas.presentation.tutorial.OnboardingFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HubItem(val label: String, val icon: ImageVector, val screen: Screen)

@HiltViewModel
class HubViewModel @Inject constructor(
    private val tutorialManager: TutorialManager
) : ViewModel() {

    private val _showOnboarding = MutableStateFlow(false)
    val showOnboarding = _showOnboarding.asStateFlow()

    init {
        viewModelScope.launch {
            _showOnboarding.value = !tutorialManager.isTutorialCompleted(TutorialModule.ONBOARDING)
        }
    }

    fun onOnboardingComplete() {
        _showOnboarding.value = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubEmpresaScreen(
    onNavigate: (Screen) -> Unit,
    onLogout: () -> Unit,
    viewModel: HubViewModel = hiltViewModel()
) {
    val showOnboarding by viewModel.showOnboarding.collectAsState()

    val items = listOf(
        HubItem("Resumen", Icons.Default.Dashboard, Screen.Dashboard),
        HubItem("Ingresar Doc.", Icons.Default.AddCircle, Screen.AddDocumento),
        HubItem("Por Pagar", Icons.Default.TrendingDown, Screen.CuentasPagar),
        HubItem("Por Cobrar", Icons.Default.TrendingUp, Screen.CuentasCobrar),
        HubItem("Cheques", Icons.Default.Receipt, Screen.Cheques),
        HubItem("Cuentas", Icons.Default.AccountBalance, Screen.Cuentas),
        HubItem("Contactos", Icons.Default.Contacts, Screen.Contactos),
        HubItem("Simulador", Icons.Default.Calculate, Screen.Simulador),
        HubItem("Opciones", Icons.Default.Settings, Screen.Opciones),
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items.dropLast(1)) { item ->
                Card(
                    onClick = { onNavigate(item.screen) },
                    modifier = Modifier.aspectRatio(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            item.icon,
                            null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            item.label,
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Opciones a todo el ancho
            item(span = { GridItemSpan(2) }) {
                val opcion = items.last()
                Card(
                    onClick = { onNavigate(opcion.screen) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            opcion.icon,
                            null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(opcion.label, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }

    // Show onboarding on first visit
    if (showOnboarding) {
        OnboardingFlow(
            onComplete = { viewModel.onOnboardingComplete() }
        )
    }
}
