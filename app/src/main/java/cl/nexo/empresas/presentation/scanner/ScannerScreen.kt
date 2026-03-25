package cl.nexo.empresas.presentation.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import cl.nexo.empresas.data.model.DteScanResult
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun ScannerScreen(
    onScanned: (DteScanResult) -> Unit,
    onBack: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val context       = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic        = LocalHapticFeedback.current
    val state         by viewModel.state.collectAsState()

    // ── Permiso de cámara ───────────────────────────────────────────────────
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // ── Navegación al detectar resultado ────────────────────────────────────
    LaunchedEffect(state) {
        if (state is ScannerState.Found) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onScanned((state as ScannerState.Found).result)
        }
    }

    // ── CameraX setup ───────────────────────────────────────────────────────
    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val barcodeScanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_PDF417)
                .build()
        )
    }

    DisposableEffect(lifecycleOwner, hasCameraPermission) {
        if (!hasCameraPermission) return@DisposableEffect onDispose {}

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null && state !is ScannerState.Found) {
                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )
                            barcodeScanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    barcodes.firstOrNull()?.rawValue?.let { raw ->
                                        TedParser.parse(raw)?.let { result ->
                                            viewModel.onBarcodeDetected(result)
                                        }
                                    }
                                }
                                .addOnCompleteListener { imageProxy.close() }
                        } else {
                            imageProxy.close()
                        }
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                viewModel.onError("Error al iniciar cámara: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            barcodeScanner.close()
        }
    }

    // ── UI ──────────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escanear Timbre DTE") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.6f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                !hasCameraPermission -> {
                    // Pantalla de permiso denegado
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "📷",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Permiso de cámara requerido",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Necesitamos acceso a la cámara para leer el código PDF417 de la factura.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Otorgar Permiso")
                        }
                    }
                }

                else -> {
                    // Vista de cámara
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay oscuro con ventana transparente
                    ScannerOverlay(
                        isDetected = state is ScannerState.Found
                    )

                    // Mensaje de estado
                    val statusText = when (state) {
                        is ScannerState.Scanning -> "Apunta al código PDF417 de la factura\n(esquina inferior del documento)"
                        is ScannerState.Found    -> "✅ Factura detectada"
                        is ScannerState.Error    -> "⚠️ ${(state as ScannerState.Error).message}"
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 48.dp)
                            .padding(horizontal = 32.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.65f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = statusText,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Overlay con máscara oscura + marco guía centrado para el PDF417.
 * El PDF417 es rectangular apaisado — el marco refleja esa proporción.
 */
@Composable
private fun ScannerOverlay(isDetected: Boolean) {
    val frameColor = if (isDetected) Color(0xFF4CAF50) else Color.White
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth  = size.width
        val canvasHeight = size.height

        // Marco guía: ancho = 80% del canvas, alto = 25% (proporción PDF417)
        val frameWidth  = canvasWidth * 0.82f
        val frameHeight = canvasWidth * 0.25f
        val frameLeft   = (canvasWidth  - frameWidth)  / 2f
        val frameTop    = (canvasHeight - frameHeight) / 2f

        // Fondo oscuro completo
        drawRect(color = Color.Black.copy(alpha = 0.55f))

        // Ventana transparente (borrar el rectángulo del marco)
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(frameLeft, frameTop),
            size = Size(frameWidth, frameHeight),
            cornerRadius = CornerRadius(8.dp.toPx()),
            blendMode = BlendMode.Clear
        )

        // Borde del marco
        drawRoundRect(
            color = frameColor,
            topLeft = Offset(frameLeft, frameTop),
            size = Size(frameWidth, frameHeight),
            cornerRadius = CornerRadius(8.dp.toPx()),
            style = Stroke(width = 3.dp.toPx())
        )

        // Esquinas decorativas
        val cornerLen = 24.dp.toPx()
        val stroke = Stroke(width = 5.dp.toPx())
        val corners = listOf(
            Offset(frameLeft, frameTop),
            Offset(frameLeft + frameWidth, frameTop),
            Offset(frameLeft, frameTop + frameHeight),
            Offset(frameLeft + frameWidth, frameTop + frameHeight)
        )
        corners.forEachIndexed { idx, corner ->
            val cornerStrokeWidth = 5.dp.toPx()
            val corners = listOf(
                Offset(frameLeft, frameTop),
                Offset(frameLeft + frameWidth, frameTop),
                Offset(frameLeft, frameTop + frameHeight),
                Offset(frameLeft + frameWidth, frameTop + frameHeight)
            )
            corners.forEachIndexed { idx, corner ->
                val signX = if (idx % 2 == 0) 1f else -1f
                val signY = if (idx < 2) 1f else -1f
                drawLine(frameColor, corner, corner.copy(x = corner.x + signX * cornerLen), strokeWidth = cornerStrokeWidth)
                drawLine(frameColor, corner, corner.copy(y = corner.y + signY * cornerLen), strokeWidth = cornerStrokeWidth)
            }
        }
    }
}