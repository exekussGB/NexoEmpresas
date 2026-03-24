package cl.nexo.empresas.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cl.nexo.empresas.presentation.auth.LoginScreen
import cl.nexo.empresas.presentation.auth.RegisterScreen
import cl.nexo.empresas.presentation.cheques.ChequesScreen
import cl.nexo.empresas.presentation.contactos.ContactosScreen
import cl.nexo.empresas.presentation.cuentas.CuentasCorrientesScreen
import cl.nexo.empresas.presentation.documentos.AddDocumentoScreen
import cl.nexo.empresas.presentation.documentos.DocumentosScreen
import cl.nexo.empresas.presentation.empresas.EmpresasScreen
import cl.nexo.empresas.presentation.graficos.GraficosScreen
import cl.nexo.empresas.presentation.hub.HubEmpresaScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Screen.Empresas.route) { popUpTo(Screen.Login.route) { inclusive = true } } },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Screen.Empresas.route) { popUpTo(Screen.Login.route) { inclusive = true } } },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable(Screen.Empresas.route) {
            EmpresasScreen(onEmpresaSelected = { navController.navigate(Screen.Hub.route) })
        }
        composable(Screen.Hub.route) {
            HubEmpresaScreen(
                onNavigate = { screen -> navController.navigate(screen.route) },
                onLogout = { navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } }
            )
        }
        composable(Screen.Contactos.route) {
            ContactosScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Cuentas.route) {
            CuentasCorrientesScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Graficos.route) {
            GraficosScreen(onBack = { navController.popBackStack() })
        }
        // CxC — Cuentas por Cobrar (ingresos)
        composable(Screen.CuentasCobrar.route) {
            DocumentosScreen(
                tipo = "ingreso",
                onBack = { navController.popBackStack() },
                onAddDocumento = { navController.navigate(Screen.AddDocumento.route) }
            )
        }
        // CxP — Cuentas por Pagar (egresos)
        composable(Screen.CuentasPagar.route) {
            DocumentosScreen(
                tipo = "egreso",
                onBack = { navController.popBackStack() },
                onAddDocumento = { navController.navigate(Screen.AddDocumento.route) }
            )
        }
        // Cheques
        composable(Screen.Cheques.route) {
            ChequesScreen(onBack = { navController.popBackStack() })
        }
        // Ingresar Documento
        composable(Screen.AddDocumento.route) {
            AddDocumentoScreen(onBack = { navController.popBackStack() })
        }
        // Pantallas aún pendientes
        composable(Screen.Dashboard.route) {
            PlaceholderScreen("Dashboard") { navController.popBackStack() }
        }
        composable(Screen.Opciones.route) {
            PlaceholderScreen("Opciones") { navController.popBackStack() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderScreen(title: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
            Text("$title — próximamente")
        }
    }
}
