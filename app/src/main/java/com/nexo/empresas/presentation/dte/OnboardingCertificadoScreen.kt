package com.nexo.empresas.dte.ui.dte

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Pantalla de onboarding para registrar el certificado digital (.pfx / .p12) de la empresa.
 *
 * IMPORTANTE: El archivo .pfx se lee en memoria, se convierte a Base64 y se envía
 * directamente a la Edge Function de Supabase (registrar-certificado), la cual lo
 * almacena en Supabase Vault cifrado por empresa. El archivo NUNCA se guarda
 * en el dispositivo ni en Supabase Storage.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingCertificadoScreen(
    empresaId: String,
    onNavigateBack: () -> Unit,
    onCertificadoRegistrado: () -> Unit,
    viewModel: DteViewModel = hiltViewModel()
) {
    val uiState by viewModel.onboardingState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var claveVisible by remember { mutableStateOf(false) }

    // Navegar al completar
    LaunchedEffect(uiState.success) {
        if (uiState.success) onCertificadoRegistrado()
    }

    // Launcher para seleccionar archivo .pfx / .p12
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val (nombre, base64) = readFileAsBase64(context, it)
            if (base64 != null) {
                viewModel.onPfxSeleccionado(nombre, base64)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Certificado Digital") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Información ────────────────────────────────────────────────
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "¿Qué es el certificado digital?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Es la firma electrónica que autentica y firma cada DTE ante el SII. " +
                            "Se obtiene de proveedores como E-CertChile, Acepta o FirmaVirtual. " +
                            "El archivo tiene extensión .pfx o .p12.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // ── Advertencia de seguridad ───────────────────────────────────
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Tu certificado se enviará cifrado al servidor y se almacenará " +
                        "en Supabase Vault. Nunca quedará guardado en este dispositivo.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // ── Selector de archivo ────────────────────────────────────────
            Text("Paso 1: Selecciona el archivo .pfx",
                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

            OutlinedButton(
                onClick = { fileLauncher.launch("application/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AttachFile, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(uiState.pfxNombreArchivo ?: "Seleccionar archivo .pfx / .p12")
            }

            if (uiState.pfxNombreArchivo != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Archivo listo: ${uiState.pfxNombreArchivo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // ── Clave del certificado ──────────────────────────────────────
            Text("Paso 2: Ingresa la clave del certificado",
                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

            OutlinedTextField(
                value = uiState.clavePfx,
                onValueChange = viewModel::onClavePfxChange,
                label = { Text("Clave del certificado (.pfx)") },
                visualTransformation = if (claveVisible) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { claveVisible = !claveVisible }) {
                        Icon(
                            if (claveVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (claveVisible) "Ocultar" else "Mostrar"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            // ── Error ──────────────────────────────────────────────────────
            uiState.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // ── Botón registrar ────────────────────────────────────────────
            Button(
                onClick = { viewModel.registrarCertificado(empresaId) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && uiState.pfxBase64 != null && uiState.clavePfx.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Registrando certificado...")
                } else {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Registrar certificado")
                }
            }

            // ── Tabla de proveedores ───────────────────────────────────────
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Proveedores de certificado digital acreditados por el SII:",
                        style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    listOf(
                        "E-CertChile" to "ecertchile.cl",
                        "Acepta" to "acepta.com",
                        "FirmaVirtual" to "firmavirtual.cl",
                        "GlobalSign" to "globalsign.com"
                    ).forEach { (nombre, sitio) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Text("• $nombre", modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall)
                            Text(sitio, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

// ─── Utilidad: leer archivo como Base64 ──────────────────────────────────────

private fun readFileAsBase64(context: Context, uri: Uri): Pair<String, String?> {
    val nombre = uri.lastPathSegment?.substringAfterLast('/') ?: "certificado.pfx"
    return try {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        nombre to (bytes?.let { Base64.encodeToString(it, Base64.NO_WRAP) })
    } catch (e: Exception) {
        nombre to null
    }
}
