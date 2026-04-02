package cl.nexo.empresas.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
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

    val hasMinLength = password.length >= 8
    val hasUppercase = password.any { it.isUpperCase() }
    val hasLowercase = password.any { it.isLowerCase() }
    val hasNumber = password.any { it.isDigit() }
    val hasSpecialChar = password.any { !it.isLetterOrDigit() }
    val allRequirementsMet = hasMinLength && hasUppercase && hasLowercase && hasNumber && hasSpecialChar
    val isValidEmail = email.contains("@") && email.contains(".")

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
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = email.isNotEmpty() && !isValidEmail
        )
        if (email.isNotEmpty() && !isValidEmail) {
            Text(
                "Ingresa un email válido",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        if (password.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp)
            ) {
                PasswordRequirement("Mínimo 8 caracteres", hasMinLength)
                PasswordRequirement("Al menos una letra mayúscula", hasUppercase)
                PasswordRequirement("Al menos una letra minúscula", hasLowercase)
                PasswordRequirement("Al menos un número", hasNumber)
                PasswordRequirement("Al menos un carácter especial (!@#\$%^&*...)", hasSpecialChar)
            }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = confirm, onValueChange = { confirm = it },
            label = { Text("Confirmar contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(24.dp))

        if (password.isNotEmpty() && confirm.isNotEmpty() && password != confirm)
            Text(
                "Las contraseñas no coinciden",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )

        if (state is AuthUiState.Error)
            Text(
                (state as AuthUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )

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
                if (password == confirm && isValidEmail && allRequirementsMet)
                    vm.register(email, password)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = state !is AuthUiState.Loading &&
                      state !is AuthUiState.EmailPendingConfirmation &&
                      isValidEmail &&
                      allRequirementsMet &&
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

@Composable
private fun PasswordRequirement(text: String, met: Boolean) {
    val color = if (met) Color(0xFF4CAF50) else Color.Gray
    val icon = if (met) "✓" else "○"
    Text(
        text = "$icon $text",
        color = color,
        style = MaterialTheme.typography.bodySmall
    )
}
