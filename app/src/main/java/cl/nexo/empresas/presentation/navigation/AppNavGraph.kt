package cl.nexo.empresas.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import cl.nexo.empresas.core.tutorial.TutorialModule
import cl.nexo.empresas.presentation.alertas.AlertasConfigScreen
import cl.nexo.empresas.presentation.auth.ForgotPasswordScreen
import cl.nexo.empresas.presentation.auth.LoginScreen
import cl.nexo.empresas.presentation.auth.RegisterScreen
import cl.nexo.empresas.presentation.auth.ResetPasswordScreen
import cl.nexo.empresas.presentation.auth.VerifyOtpScreen
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
import cl.nexo.empresas.presentation.settings.TeamMembersScreen
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
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
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

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBackToLogin = { navController.popBackStack() },
                onNavigateToVerifyOtp = { email ->
                    navController.navigate(Screen.VerifyOtp.route(email))
                }
            )
        }

        composable(
            route = Screen.VerifyOtp.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("email") ?: "",
                "UTF-8"
            )
            VerifyOtpScreen(
                email = email,
                onVerified = { navController.navigate(Screen.ResetPassword.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ResetPassword.route) {
            ResetPasswordScreen(
                onResetSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
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
            ModuleTutorialLauncher(TutorialModule.EMPRESA_SETUP)
        }

        composable(Screen.Hub.route) {
            HubEmpresaScreen(
                onNavigate = { screen -> navController.navigate(screen.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBackToEmpresas = {
                    navController.navigate(Screen.Empresas.route) {
                        popUpTo(Screen.Hub.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onBack = { navController.popBackStack() },
                onVerGraficos = { navController.navigate(Screen.Graficos.route) }
            )
            ModuleTutorialLauncher(TutorialModule.RESUMEN)
        }

        composable(Screen.AddDocumento.route) {
            AddDocumentoScreen(
                navController = navController,
                onBack = { navController.popBackStack() }
            )
            ModuleTutorialLauncher(TutorialModule.INGRESAR_DOC)
        }

        composable(Screen.CuentasCobrar.route) {
            DocumentosScreen(
                tipo = "ingreso",
                onBack = { navController.popBackStack() },
                onAddDocumento = { navController.navigate(Screen.AddDocumento.route) }
            )
            ModuleTutorialLauncher(TutorialModule.POR_COBRAR)
        }

        composable(Screen.CuentasPagar.route) {
            DocumentosScreen(
                tipo = "egreso",
                onBack = { navController.popBackStack() },
                onAddDocumento = { navController.navigate(Screen.AddDocumento.route) }
            )
            ModuleTutorialLauncher(TutorialModule.POR_PAGAR)
        }

        composable(Screen.Cheques.route) {
            ChequesScreen(
                onBack = { navController.popBackStack() }
            )
            ModuleTutorialLauncher(TutorialModule.CHEQUES)
        }

        composable(Screen.Graficos.route) {
            GraficosScreen(
                onBack = { navController.popBackStack() }
            )
            ModuleTutorialLauncher(TutorialModule.GRAFICOS)
        }

        composable(Screen.Cuentas.route) {
            CuentasCorrientesScreen(
                onBack = { navController.popBackStack() }
            )
            ModuleTutorialLauncher(TutorialModule.CUENTAS)
        }

        composable(Screen.Contactos.route) {
            ContactosScreen(
                onBack = { navController.popBackStack() }
            )
            ModuleTutorialLauncher(TutorialModule.CONTACTOS)
        }

        composable(Screen.Simulador.route) {
            SimuladorScreen(
                onBack = { navController.popBackStack() }
            )
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
                onTutoriales = { navController.navigate(Screen.TutorialList.route) },
                onTeamMembers = { navController.navigate(Screen.TeamMembers.route) }
            )
            ModuleTutorialLauncher(TutorialModule.EMPRESA_MIEMBROS)
        }

        composable(Screen.Alertas.route) {
            AlertasConfigScreen(
                onBack = { navController.popBackStack() }
            )
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
            ModuleTutorialLauncher(TutorialModule.SCANNER)
        }

        composable(Screen.TutorialList.route) {
            TutorialListScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TeamMembers.route) {
            TeamMembersScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
