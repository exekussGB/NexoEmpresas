package cl.nexo.empresas.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cl.nexo.empresas.presentation.auth.LoginScreen
import cl.nexo.empresas.presentation.auth.RegisterScreen
import cl.nexo.empresas.presentation.empresas.EmpresasScreen
import cl.nexo.empresas.presentation.hub.HubEmpresaScreen
import cl.nexo.empresas.presentation.dashboard.DashboardScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Screen.Empresas.route) { popUpTo(0) } },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Screen.Empresas.route) { popUpTo(0) } },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable(Screen.Empresas.route) {
            EmpresasScreen(
                onEmpresaSelected = { navController.navigate(Screen.Hub.route) }
            )
        }
        composable(Screen.Hub.route) {
            HubEmpresaScreen(
                onNavigate = { screen -> navController.navigate(screen.route) },
                onLogout = { navController.navigate(Screen.Login.route) { popUpTo(0) } }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(onBack = { navController.popBackStack() })
        }
        // TODO: agregar composables de cada pantalla en orden de implementación
    }
}
