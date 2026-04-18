package com.nexo.empresas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.nexo.empresas.presentation.navigation.AppNavGraph
import com.nexo.empresas.presentation.navigation.Screen
import com.nexo.empresas.presentation.auth.AuthViewModel
import com.nexo.empresas.presentation.theme.NexoEmpresasTheme
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
                val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

                AppNavGraph(
                    navController = navController,
                    startDestination = if (isLoggedIn) Screen.Empresas.route else Screen.Login.route
                )
            }
        }
    }
}
