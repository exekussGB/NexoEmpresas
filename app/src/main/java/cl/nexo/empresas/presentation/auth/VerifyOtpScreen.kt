package cl.nexo.empresas.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyOtpScreen(
    email: String,
    onVerified: () -> Unit,
    onBack: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    var code by remember { mutableStateOf("") }
    val state by vm.otpState.collectAsState()

    LaunchedEffect(state) {
        if (state is AuthUiState.OtpVerified) {
            onVerified()
        }
    }

    DisposableEffect(Unit) {
        onDispose { vm.resetOtpState() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verificar código") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
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
                "Ingresa el código",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Enviamos un código de 8 dígitos a:",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                email,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = code,
                onValueChange = { newValue ->
                    if (newValue.length <= 8 && newValue.all { it.isDigit() }) {
                        code = newValue
                    }
                },
                label = { Text("Código de verificación") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("000000") }
            )
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
                onClick = { vm.verifyOtp(email, code) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = code.length == 8 && state !is AuthUiState.Loading
            ) {
                if (state is AuthUiState.Loading)
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else
                    Text("Verificar código")
            }
        }
    }
}
