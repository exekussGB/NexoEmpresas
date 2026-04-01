package cl.nexo.empresas.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cl.nexo.empresas.core.tutorial.TutorialModule
import cl.nexo.empresas.presentation.alertas.AlertasConfigScreen
import cl.nexo.empresas.presentation.auth.LoginScreen
import cl.nexo.empresas.presentation.auth.RegisterScreen
import cl.nexo.empresas.presentation.cheques.ChequesScreen
import cl.nexo.empresas.presentation.contactos.ContactosScreen
import cl.nexo.empresas.presentation.cuentas.CuentasCorrientesScreen
import cl.nexo.empresas.presentation.dashboard.DashboardScreen
import cl.nexo.empresas.presentation.documentos.AddDocumentoScreen
import cl.nexo.empresas.presentation.documentos.DocumentosScreen
import cl.nexo.empresas.presentation.empresas.EmpresasScreen
import cl.nexo.empresas.presentation.graficos.GraficosScreen
import cl.nexo.empresas.presentation.hub.HubEmpresaScreen
import cl.nexo.empresas.presentation.settings.SettingsScreen
import cl.nexo.empresas.presentation.scanner.ScannerScreen
import cl.nexo.empresas.presentation.simulador.SimuladorScreen
import cl.nexo.empresas.presentation.tutorial.ModuleTutorialLauncher
import cl.nexo.empresas.presentation.tutorial.TutorialListScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
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

        composable(Screen.Empresas.route) {
            EmpresasScreen(
                onEmpresaSelected = {
                    navController.navigate(Screen.Hub.route) {
                        popUpTo(Screen.Empresas.route) { inclusive = true }
                    }
                }
            )
            // Tutorial: Crear Empresa
            ModuleTutorialLauncher(TutorialModule.EMPRESA_SETUP)
        }

        composable(Screen.Hub.route) {
            HubEmpresaScreen(
                onNavigate = { screen -> navController.navigate(screen.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
            // Onboarding + Hub tutorials are handled in HubEmpresaScreen itself
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onBack = { navController.popBackStack() },
                onVerGraficos = { navController.navigate(Screen.Graficos.route) }
            )
            // Tutorial: Resumen Financiero
            ModuleTutorialLauncher(TutorialModule.RESUMEN)
        }

        composable(Screen.AddDocumento.route) {
            AddDocumentoScreen(
                navController = navController,
                onBack = { navController.popBackStack() }
            )
            // Tutorial: Ingresar Documento
            ModuleTutorialLauncher(TutorialModule.INGRESAR_DOC)
        }

        composable(Screen.CuentasCobrar.route) {
            DocumentosScreen(
                tipo = "ingreso",
                onBack = { navController.popBackStack() },
                onAddDocumento = { navController.navigate(Screen.AddDocumento.route) }
            )
            // Tutorial: Por Cobrar
            ModuleTutorialLauncher(TutorialModule.POR_COBRAR)
        }

        composable(Screen.CuentasPagar.route) {
            DocumentosScreen(
                tipo = "egreso",
                onBack = { navController.popBackStack() },
                onAddDocumento = { navController.navigate(Screen.AddDocumento.route) }
            )
            // Tutorial: Por Pagar
            ModuleTutorialLauncher(TutorialModule.POR_PAGAR)
        }

        composable(Screen.Cheques.route) {
            ChequesScreen(
                onBack = { navController.popBackStack() }
            )
            // Tutorial: Cheques
            ModuleTutorialLauncher(TutorialModule.CHEQUES)
        }

        composable(Screen.Graficos.route) {
            GraficosScreen(
                onBack = { navController.popBackStack() }
            )
            // Tutorial: Gráficos
            ModuleTutorialLauncher(TutorialModule.GRAFICOS)
        }

        composable(Screen.Cuentas.route) {
            CuentasCorrientesScreen(
                onBack = { navController.popBackStack() }
            )
            // Tutorial: Cuentas Corrientes
            ModuleTutorialLauncher(TutorialModule.CUENTAS)
        }

        composable(Screen.Contactos.route) {
            ContactosScreen(
                onBack = { navController.popBackStack() }
            )
            // Tutorial: Contactos
            ModuleTutorialLauncher(TutorialModule.CONTACTOS)
        }

        composable(Screen.Simulador.route) {
            SimuladorScreen(
                onBack = { navController.popBackStack() }
            )
            // Tutorial: Simulador de Contratación
            ModuleTutorialLauncher(TutorialModule.SIMULADOR)
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
            // Tutorial: Miembros y Roles
            ModuleTutorialLauncher(TutorialModule.EMPRESA_MIEMBROS)
        }

        composable(Screen.Alertas.route) {
            AlertasConfigScreen(
                onBack = { navController.popBackStack() }
            )
            // Tutorial: Alertas
            ModuleTutorialLauncher(TutorialModule.ALERTAS)
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
            // Tutorial: Escáner DTE
            ModuleTutorialLauncher(TutorialModule.SCANNER)
        }

        composable(Screen.TutorialList.route) {
            TutorialListScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
