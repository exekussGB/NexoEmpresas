package cl.nexo.empresas.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit,
    onNavigateToVerifyOtp: (String) -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    val state by vm.forgotState.collectAsState()
    val isValidEmail = email.contains("@")

    LaunchedEffect(state) {
        if (state is AuthUiState.ResetEmailSent) {
            onNavigateToVerifyOtp(email)
        }
    }

    DisposableEffect(Unit) {
        onDispose { vm.resetForgotState() }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Recuperar contraseña",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Ingresa tu email y te enviaremos un código para restablecer tu contraseña.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
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
            onClick = { vm.sendPasswordReset(email) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = isValidEmail && state !is AuthUiState.Loading
        ) {
            if (state is AuthUiState.Loading)
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else
                Text("Enviar código de recuperación")
        }
        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onBackToLogin) {
            Text("Volver a iniciar sesión")
        }
    }
}
