package cl.nexo.empresas.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cl.nexo.empresas.presentation.auth.LoginScreen
import cl.nexo.empresas.presentation.auth.RegisterScreen
import cl.nexo.empresas.presentation.contactos.ContactosScreen
import cl.nexo.empresas.presentation.cuentas.CuentasCorrientesScreen
import cl.nexo.empresas.presentation.empresas.EmpresasScreen
import cl.nexo.empresas.presentation.hub.HubEmpresaScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(navController = navController, startDestination = startDestination) {

        // Auth
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Empresas.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Empresas.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // Selección de empresa
        composable(Screen.Empresas.route) {
            EmpresasScreen(
                onEmpresaSelected = { navController.navigate(Screen.Hub.route) }
            )
        }

// Hub de empresa
        composable(Screen.Hub.route) {
            HubEmpresaScreen(
                onNavigate = { screen -> navController.navigate(screen.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Contactos
        composable(Screen.Contactos.route) {
            ContactosScreen(onBack = { navController.popBackStack() })
        }

        // Cuentas Corrientes
        composable(Screen.Cuentas.route) {
            CuentasCorrientesScreen(onBack = { navController.popBackStack() })
        }

        // Pantallas pendientes
        composable(Screen.Dashboard.route) {
            PlaceholderScreen("Dashboard") { navController.popBackStack() }
        }
        composable(Screen.CuentasCobrar.route) {
            PlaceholderScreen("Por Cobrar") { navController.popBackStack() }
        }
        composable(Screen.CuentasPagar.route) {
            PlaceholderScreen("Por Pagar") { navController.popBackStack() }
        }
        composable(Screen.Cheques.route) {
            PlaceholderScreen("Cheques") { navController.popBackStack() }
        }
        composable(Screen.AddDocumento.route) {
            PlaceholderScreen("Ingresar Documento") { navController.popBackStack() }
        }
        composable(Screen.Graficos.route) {
            PlaceholderScreen("Gráficos") { navController.popBackStack() }
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
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("$title — próximamente")
        }
    }
}
