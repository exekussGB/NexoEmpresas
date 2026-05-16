package com.nexo.empresas.presentation.hub

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexo.empresas.R
import com.nexo.empresas.core.tutorial.TutorialManager
import com.nexo.empresas.core.session.TenantManager
import com.nexo.empresas.core.tutorial.TutorialModule
import com.nexo.empresas.presentation.navigation.Screen
import com.nexo.empresas.presentation.tutorial.OnboardingFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HubItem(val label: String, val icon: ImageVector, val screen: Screen)

@HiltViewModel
class HubViewModel @Inject constructor(
    private val tutorialManager: TutorialManager,
    val tenantManager: TenantManager
) : ViewModel() {

    private val _showOnboarding = MutableStateFlow(false)
    val showOnboarding = _showOnboarding.asStateFlow()

    val currentEmpresaId: String? get() = tenantManager.currentEmpresaId

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
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: HubViewModel = hiltViewModel()
) {
    val showOnboarding by viewModel.showOnboarding.collectAsState()

    // ── Grid 2×2: módulos de uso frecuente ───────────────────────────────────
    val gridItems = listOf(
        HubItem("Resumen",         Icons.Default.Dashboard,                Screen.Dashboard),
        HubItem("Ingresar Doc.",   Icons.Default.AddCircle,                Screen.AddDocumento),
        HubItem("Por Cobrar",      Icons.AutoMirrored.Filled.TrendingUp,   Screen.CuentasCobrar),
        HubItem("Por Pagar",       Icons.AutoMirrored.Filled.TrendingDown, Screen.CuentasPagar),
        HubItem("Cheques",         Icons.Default.Receipt,                  Screen.Cheques),
        HubItem("Cuentas",         Icons.Default.AccountBalance,           Screen.Cuentas),
        HubItem("Facturación SII", Icons.Default.Receipt,                  Screen.DteRoot),
        HubItem("Contactos",       Icons.Default.Contacts,                 Screen.Contactos),
    )

    // ── Ancho completo: accesos secundarios ───────────────────────────────────
    val fullWidthItems = listOf(
        HubItem("Contrataciones", Icons.Default.Work,     Screen.Contrataciones),
        HubItem("Opciones",       Icons.Default.Settings, Screen.Opciones),
    )

    // ── Resuelve la ruta de navegación según la pantalla ─────────────────────
    fun resolveRoute(item: HubItem): String? = when (item.screen) {
        Screen.DteRoot -> viewModel.currentEmpresaId?.let { Screen.DteLista.route(it) }
        else -> item.screen.route
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("NexoEmpresas")
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Cerrar sesión")
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
            // ── Tarjetas cuadradas 2×2 ────────────────────────────────────────
            items(gridItems) { item ->
                Card(
                    onClick = { resolveRoute(item)?.let(onNavigate) },
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
                            contentDescription = null,
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

            // ── Tarjetas de ancho completo ────────────────────────────────────
            items(
                fullWidthItems,
                span = { GridItemSpan(2) }
            ) { item ->
                Card(
                    onClick = { resolveRoute(item)?.let(onNavigate) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            item.label,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }

    if (showOnboarding) {
        OnboardingFlow(
            onComplete = { viewModel.onOnboardingComplete() }
        )
    }
}
