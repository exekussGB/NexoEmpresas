package cl.nexo.empresas.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cl.nexo.empresas.presentation.alertas.AlertasConfigScreen
import cl.nexo.empresas.presentation.auth.LoginScreen
import cl.nexo.empresas.presentation.auth.RegisterScreen
import cl.nexo.empresas.presentation.cheques.ChequesScreen
import cl.nexo.empresas.presentation.contactos.ContactosScreen
import cl.nexo.empresas.presentation.cuentas.CuentasCorrientesScreen
import cl.nexo.empresas.presentation.dashboard.DashboardScreen
import cl.nexo.empresas.presentation.documentos.AddDocumentoScreen
import cl.nexo.empresas.presentation.documentos.CuentasCobrarScreen
import cl.nexo.empresas.presentation.documentos.CuentasPagarScreen
import cl.nexo.empresas.presentation.documentos.DocumentoDetailScreen
import cl.nexo.empresas.presentation.empresas.EmpresasScreen
import cl.nexo.empresas.presentation.graficos.GraficosScreen
import cl.nexo.empresas.presentation.hub.HubScreen
import cl.nexo.empresas.presentation.settings.SettingsScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
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
            EmpresasScreen(
                onEmpresaSelected = { navController.navigate(Screen.Hub.route) { popUpTo(Screen.Empresas.route) { inclusive = true } } }
            )
        }

        composable(Screen.Hub.route) {
            HubScreen(
                onNavigate = { screen -> navController.navigate(screen.route) }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onBack = { navController.popBackStack() },
                onVerGraficos = { navController.navigate(Screen.Graficos.route) }
            )
        }

        composable(Screen.CuentasCobrar.route) {
            CuentasCobrarScreen(
                onBack = { navController.popBackStack() },
                onAddDocumento = { navController.navigate(Screen.AddDocumento.route) },
                onDocumentoClick = { id -> navController.navigate(Screen.DocumentoDetail.route(id)) }
            )
        }

        composable(Screen.CuentasPagar.route) {
            CuentasPagarScreen(
                onBack = { navController.popBackStack() },
                onAddDocumento = { navController.navigate(Screen.AddDocumento.route) },
                onDocumentoClick = { id -> navController.navigate(Screen.DocumentoDetail.route(id)) }
            )
        }

        composable(Screen.Cheques.route) {
            ChequesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AddDocumento.route) {
            AddDocumentoScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(Screen.Graficos.route) {
            GraficosScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Cuentas.route) {
            CuentasCorrientesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Contactos.route) {
            ContactosScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.DocumentoDetail.route) {
            val documentoId = it.arguments?.getString("documentoId") ?: ""
            DocumentoDetailScreen(
                documentoId = documentoId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Opciones.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onAlertas = { navController.navigate(Screen.Alertas.route) }
            )
        }

        composable(Screen.Alertas.route) {
            AlertasConfigScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
