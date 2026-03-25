package cl.nexo.empresas.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm  by remember { mutableStateOf("") }
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) {
        vm.uiState.collectLatest { if (it is AuthUiState.Success) onRegisterSuccess() }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crear cuenta", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = confirm, onValueChange = { confirm = it },
            label = { Text("Confirmar contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(24.dp))

        // Passwords no coinciden
        if (password.isNotEmpty() && confirm.isNotEmpty() && password != confirm)
            Text(
                "Las contraseñas no coinciden",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )

        // Error de registro
        if (state is AuthUiState.Error)
            Text(
                (state as AuthUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )

        // Email pendiente de confirmación
        if (state is AuthUiState.EmailPendingConfirmation)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "¡Cuenta creada!",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Revisa tu email para confirmar tu cuenta y luego inicia sesión.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (password == confirm && email.isNotBlank() && password.isNotBlank())
                    vm.register(email, password)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = state !is AuthUiState.Loading &&
                      state !is AuthUiState.EmailPendingConfirmation &&
                      email.isNotBlank() &&
                      password.isNotBlank() &&
                      password == confirm
        ) {
            if (state is AuthUiState.Loading)
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else
                Text("Registrarse")
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onNavigateToLogin) { Text("¿Ya tienes cuenta? Inicia sesión") }
    }
}
