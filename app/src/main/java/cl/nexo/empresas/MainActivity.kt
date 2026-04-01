package cl.nexo.empresas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import cl.nexo.empresas.presentation.navigation.AppNavGraph
import cl.nexo.empresas.presentation.navigation.Screen
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
                val authViewModel: AuthViewModel = hiltViewModel()
                val isLoggedIn by authViewModel.isLoggedIn

                when (isLoggedIn) {
                    null -> {
                        // Still checking session — show loading
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Text("Cargando...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    true -> {
                        // Session valid → go directly to Empresas (skip login)
                        val navController = rememberNavController()
                        AppNavGraph(
                            navController = navController,
                            startDestination = Screen.Empresas.route
                        )
                    }
                    false -> {
                        // No session → show login
                        val navController = rememberNavController()
                        AppNavGraph(
                            navController = navController,
                            startDestination = Screen.Login.route
                        )
                    }
                }
            }
        }
    }
}
