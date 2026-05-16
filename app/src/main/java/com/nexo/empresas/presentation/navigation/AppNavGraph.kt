package com.nexo.empresas.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nexo.empresas.presentation.alertas.AlertasConfigScreen
import com.nexo.empresas.presentation.auth.LoginScreen
import com.nexo.empresas.presentation.auth.RegisterScreen
import com.nexo.empresas.presentation.cheques.ChequesScreen
import com.nexo.empresas.presentation.contactos.ContactosScreen
import com.nexo.empresas.presentation.contrataciones.ContratacionesScreen
import com.nexo.empresas.presentation.cuentas.CuentasCorrientesScreen
import com.nexo.empresas.presentation.dashboard.DashboardScreen
import com.nexo.empresas.presentation.documentos.AddDocumentoScreen
import com.nexo.empresas.presentation.documentos.DocumentosScreen
import com.nexo.empresas.presentation.empresas.EmpresasScreen
import com.nexo.empresas.presentation.graficos.GraficosScreen
import com.nexo.empresas.presentation.hub.HubEmpresaScreen
import com.nexo.empresas.presentation.navigation.dte.dteNavGraph
import com.nexo.empresas.presentation.settings.SettingsScreen
import com.nexo.empresas.presentation.scanner.ScannerScreen
import com.nexo.empresas.presentation.simulador.FiniquitoScreen
import com.nexo.empresas.presentation.simulador.MultiTrabajadorScreen
import com.nexo.empresas.presentation.simulador.SimuladorScreen
import com.nexo.empresas.presentation.tutorial.TutorialListScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Módulo DTE
        dteNavGraph(navController)

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
            HubEmpresaScreen(
                onNavigate = { route -> navController.navigate(route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onBack = { navController.popBackStack() },
                onVerGraficos = { navController.navigate(Screen.Graficos.route) }
            )
        }

        composable(Screen.CuentasCobrar.route) {
            DocumentosScreen(
                tipo = "ingreso",
                onBack = { navController.popBackStack() },
                onAddDocumento = { navController.navigate(Screen.AddDocumento.route) }
            )
        }

        composable(Screen.CuentasPagar.route) {
            DocumentosScreen(
                tipo = "egreso",
                onBack = { navController.popBackStack() },
                onAddDocumento = { navController.navigate(Screen.AddDocumento.route) }
            )
        }

        composable(Screen.Cheques.route) {
            ChequesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AddDocumento.route) {
            AddDocumentoScreen(
                navController = navController,
                onBack = { navController.popBackStack() }
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

        // ── Hub intermedio de Contrataciones ─────────────────────────────────
        composable(Screen.Contrataciones.route) {
            ContratacionesScreen(
                onBack = { navController.popBackStack() },
                onNavigateToSimulador = { navController.navigate(Screen.Simulador.route) },
                onNavigateToFiniquito = { navController.navigate(Screen.Finiquito.route) },
                onNavigateToMultiTrabajador = { navController.navigate(Screen.MultiTrabajador.route(0L)) }
            )
        }

        composable(Screen.Simulador.route) {
            SimuladorScreen(
                onBack = { navController.popBackStack() },
                onNavigateToFiniquito = { navController.navigate(Screen.Finiquito.route) },
                onNavigateToMulti = { costo -> navController.navigate(Screen.MultiTrabajador.route(costo)) }
            )
        }

        composable(Screen.Finiquito.route) {
            FiniquitoScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.MultiTrabajador.route,
            arguments = listOf(navArgument("costo") { type = NavType.LongType; defaultValue = 0L })
        ) { backStackEntry ->
            val costo = backStackEntry.arguments?.getLong("costo") ?: 0L
            MultiTrabajadorScreen(
                onBack = { navController.popBackStack() },
                initialCosto = costo
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
                onAlertas = { navController.navigate(Screen.Alertas.route) },
                onTutoriales = { navController.navigate(Screen.TutorialList.route) }
            )
        }

        composable(Screen.Alertas.route) {
            AlertasConfigScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Scanner.route) {
            ScannerScreen(
                onScanned = { result ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("dte_scan_result", result)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TutorialList.route) {
            TutorialListScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
