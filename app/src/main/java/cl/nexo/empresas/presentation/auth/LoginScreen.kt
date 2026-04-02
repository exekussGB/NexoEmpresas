package cl.nexo.empresas.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import cl.nexo.empresas.R
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by vm.uiState.collectAsState()
    val isValidEmail = email.isEmpty() || email.contains("@")

    LaunchedEffect(Unit) {
        vm.uiState.collectLatest { if (it is AuthUiState.Success) onLoginSuccess() }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_nexo),
            contentDescription = "Logo NexoEmpresas",
            modifier = Modifier.size(120.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("NexoEmpresas", fontSize = 28.sp, fontWeight = FontWeight.Bold,
             color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text("Gestión financiera para tu empresa", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(40.dp))

        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = !isValidEmail
        )
        if (!isValidEmail) {
            Text(
                "Ingresa un email válido",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = password, onValueChange = { password = it },
            label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth(),
            singleLine = true, visualTransformation = PasswordVisualTransformation())
        Spacer(Modifier.height(24.dp))

        if (state is AuthUiState.Error)
            Text((state as AuthUiState.Error).message, color = MaterialTheme.colorScheme.error,
                 style = MaterialTheme.typography.bodySmall)

        Button(
            onClick = { vm.login(email, password) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = state !is AuthUiState.Loading && email.contains("@") && password.isNotBlank()
        ) {
            if (state is AuthUiState.Loading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else Text("Iniciar sesión")
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onNavigateToRegister) { Text("¿No tienes cuenta? Regístrate") }
        TextButton(onClick = onNavigateToForgotPassword) { Text("¿Olvidaste tu contraseña?") }
    }
}
