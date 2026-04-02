package cl.nexo.empresas.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    onResetSuccess: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    val state by vm.resetPasswordState.collectAsState()

    val hasMinLength = password.length >= 8
    val hasUppercase = password.any { it.isUpperCase() }
    val hasLowercase = password.any { it.isLowerCase() }
    val hasNumber = password.any { it.isDigit() }
    val hasSpecialChar = password.any { !it.isLetterOrDigit() }
    val allRequirementsMet = hasMinLength && hasUppercase && hasLowercase && hasNumber && hasSpecialChar
    val passwordsMatch = password == confirm && confirm.isNotEmpty()

    LaunchedEffect(state) {
        if (state is AuthUiState.PasswordResetSuccess) {
            onResetSuccess()
        }
    }

    DisposableEffect(Unit) {
        onDispose { vm.resetResetPasswordState() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva contraseña") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Crear nueva contraseña",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Ingresa tu nueva contraseña para completar la recuperación.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Nueva contraseña") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                }
            )

            if (password.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp)
                ) {
                    PasswordRequirementItem("Mínimo 8 caracteres", hasMinLength)
                    PasswordRequirementItem("Al menos una letra mayúscula", hasUppercase)
                    PasswordRequirementItem("Al menos una letra minúscula", hasLowercase)
                    PasswordRequirementItem("Al menos un número", hasNumber)
                    PasswordRequirementItem("Al menos un carácter especial (!@#\$%^&*...)", hasSpecialChar)
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = { Text("Confirmar contraseña") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmVisible = !confirmVisible }) {
                        Icon(
                            imageVector = if (confirmVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (confirmVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                isError = confirm.isNotEmpty() && !passwordsMatch
            )

            if (confirm.isNotEmpty() && !passwordsMatch) {
                Text(
                    "Las contraseñas no coinciden",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            if (state is AuthUiState.Error) {
                Text(
                    (state as AuthUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = { vm.resetPasswordWithNew(password) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = allRequirementsMet && passwordsMatch && state !is AuthUiState.Loading
            ) {
                if (state is AuthUiState.Loading)
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else
                    Text("Cambiar contraseña")
            }
        }
    }
}

@Composable
private fun PasswordRequirementItem(text: String, met: Boolean) {
    val color = if (met) Color(0xFF4CAF50) else Color.Gray
    val icon = if (met) "✓" else "○"
    Text(
        text = "$icon $text",
        color = color,
        style = MaterialTheme.typography.bodySmall
    )
}
