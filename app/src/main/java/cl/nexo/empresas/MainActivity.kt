package cl.nexo.empresas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import cl.nexo.empresas.core.navigation.AppNavGraph
import cl.nexo.empresas.core.navigation.Screen
import cl.nexo.empresas.presentation.auth.AuthViewModel
import cl.nexo.empresas.presentation.theme.NexoEmpresasTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexoEmpresasTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = hiltViewModel()
                val isLoggedIn by authViewModel.isLoggedIn

                AppNavGraph(
                    navController = navController,
                    startDestination = if (isLoggedIn) Screen.Empresas.route else Screen.Login.route
                )
            }
        }
    }
}
