package cl.nexo.empresas.presentation.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import cl.nexo.empresas.data.model.DteScanResult
import cl.nexo.empresas.core.tutorial.TutorialModule
import cl.nexo.empresas.presentation.tutorial.ModuleTutorialLauncher
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.LuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.pdf417.PDF417Reader
import java.io.ByteArrayOutputStream

private const val TAG = "ScannerScreen"

@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun ScannerScreen(
    onScanned: (DteScanResult) -> Unit,
    onBack: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic         = LocalHapticFeedback.current
    val state          by viewModel.state.collectAsState()

    // ── Gallery state ─────────────────────────────────────────────────────
    var isProcessingGallery by remember { mutableStateOf(false) }

    // ── Frame counter ────────────────────────────────────────────────────
    var framesAnalyzed by remember { mutableIntStateOf(0) }

    // ── Helper: process raw barcode content ─────────────────────────────
    fun processRawBarcode(raw: String, rawBytes: ByteArray? = null, source: String = "PDF417"): Boolean {
        Log.d(TAG, "[$source] Intentando parsear. rawValue length=${raw.length}, rawBytes size=${rawBytes?.size}")

        // 1) Intentar con rawValue (String)
        var result = TedParser.parse(raw)

        // 2) Si falla, intentar con rawBytes y múltiples encodings
        if (result == null && rawBytes != null) {
            Log.d(TAG, "[$source] rawValue no parseó, intentando rawBytes...")
            result = TedParser.parseFromBytes(rawBytes)
        }

        return if (result != null) {
            Log.d(TAG, "[$source] ✅ TED parseado: tipo=${result.tipoDocumento}, folio=${result.folio}")
            viewModel.onBarcodeDetected(result)
            true
        } else {
            val reason = TedParser.diagnose(raw)
            Log.d(TAG, "[$source] ❌ No es TED: $reason")
            viewModel.onParseFailed("Código detectado pero no contiene un Timbre Electrónico (TED).\n$reason")
            false
        }
    }

    // ── Helper: YUV420 to Bitmap for ZXing ──────────────────────────────
    fun yuv420ToBitmap(imageProxy: ImageProxy): Bitmap? {
        return try {
            val yBuffer = imageProxy.planes[0].buffer
            val uBuffer = imageProxy.planes[1].buffer
            val vBuffer = imageProxy.planes[2].buffer
            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()
            val nv21 = ByteArray(ySize + uSize + vSize)
            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)
            val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 90, out)
            BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())
        } catch (e: Exception) {
            Log.w(TAG, "YUV to Bitmap failed", e)
            null
        }
    }

    // ── Helper: ZXing PDF417 decode ─────────────────────────────────────
    fun tryZxingDecode(bitmap: Bitmap): String? {
        return try {
            val w = bitmap.width
            val h = bitmap.height
            val pixels = IntArray(w * h)
            bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
            val source: LuminanceSource = RGBLuminanceSource(w, h, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val hints = mapOf(
                DecodeHintType.TRY_HARDER to true,
                DecodeHintType.PURE_BARCODE to false
            )
            val reader = PDF417Reader()
            val result = reader.decode(binaryBitmap, hints)
            Log.d(TAG, "[ZXing] Decoded PDF417: ${result.text.take(100)}...")
            result.text
        } catch (e: Exception) {
            null
        }
    }

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

    // ── Auto-reset tras ParseFailed ─────────────────────────────────────
    LaunchedEffect(state) {
        if (state is ScannerState.ParseFailed) {
            kotlinx.coroutines.delay(4000)
            viewModel.reset()
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

    // Scanner para cámara en vivo: solo PDF417 + QR
    val liveBarcodeScanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_PDF417, Barcode.FORMAT_QR_CODE)
                .build()
        )
    }

    // Scanner para galería: TODOS los formatos
    val galleryBarcodeScanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_PDF417,
                    Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_DATA_MATRIX,
                    Barcode.FORMAT_AZTEC,
                    Barcode.FORMAT_CODE_128,
                    Barcode.FORMAT_CODE_39,
                    Barcode.FORMAT_CODE_93,
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_ITF,
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E,
                    Barcode.FORMAT_CODABAR
                )
                .build()
        )
    }

    // ── Gallery picker ────────────────────────────────────────────────────
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            isProcessingGallery = true
            viewModel.reset()
            try {
                val image = InputImage.fromFilePath(context, uri)

                galleryBarcodeScanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        if (barcodes.isNotEmpty()) {
                            val sorted = barcodes.sortedBy { barcode ->
                                when (barcode.format) {
                                    Barcode.FORMAT_PDF417  -> 0
                                    Barcode.FORMAT_QR_CODE -> 1
                                    else                    -> 2
                                }
                            }

                            var parsed = false
                            for (barcode in sorted) {
                                val raw = barcode.rawValue ?: ""
                                val bytes = barcode.rawBytes
                                // Intentar con rawValue + rawBytes
                                var result = TedParser.parse(raw)
                                if (result == null && bytes != null) {
                                    result = TedParser.parseFromBytes(bytes)
                                }
                                if (result != null) {
                                    viewModel.onBarcodeDetected(result)
                                    parsed = true
                                    break
                                }
                            }

                            if (!parsed) {
                                // ── ZXing fallback para galería ──
                                try {
                                    val inputStream = context.contentResolver.openInputStream(uri)
                                    val galleryBitmap = BitmapFactory.decodeStream(inputStream)
                                    inputStream?.close()
                                    if (galleryBitmap != null) {
                                        val zxRaw = tryZxingDecode(galleryBitmap)
                                        if (zxRaw != null) {
                                            val zxResult = TedParser.parse(zxRaw)
                                            if (zxResult != null) {
                                                viewModel.onBarcodeDetected(zxResult)
                                                parsed = true
                                            }
                                        }
                                        galleryBitmap.recycle()
                                    }
                                } catch (e: Exception) {
                                    Log.w(TAG, "ZXing gallery fallback failed", e)
                                }
                            }

                            if (!parsed) {
                                val firstBarcode = sorted.first()
                                val firstRaw = firstBarcode.rawValue ?: ""
                                // Último intento con rawBytes del primer barcode
                                val bytesResult = TedParser.parseFromBytes(firstBarcode.rawBytes)
                                if (bytesResult != null) {
                                    viewModel.onBarcodeDetected(bytesResult)
                                } else {
                                    val formatName = when (firstBarcode.format) {
                                        Barcode.FORMAT_PDF417      -> "PDF417"
                                        Barcode.FORMAT_QR_CODE     -> "QR"
                                        Barcode.FORMAT_DATA_MATRIX -> "DataMatrix"
                                        Barcode.FORMAT_CODE_128    -> "Code128"
                                        Barcode.FORMAT_EAN_13      -> "EAN-13"
                                        else                        -> "código"
                                    }
                                    val reason = TedParser.diagnose(firstRaw)
                                    viewModel.onParseFailed("Se detectó un $formatName pero no es una factura DTE.\n$reason")
                                }
                            }
                        } else {
                            // ── No barcodes from ML Kit → try ZXing ──
                            try {
                                val inputStream = context.contentResolver.openInputStream(uri)
                                val galleryBitmap = BitmapFactory.decodeStream(inputStream)
                                inputStream?.close()
                                if (galleryBitmap != null) {
                                    val zxRaw = tryZxingDecode(galleryBitmap)
                                    if (zxRaw != null) {
                                        val zxResult = TedParser.parse(zxRaw)
                                        if (zxResult != null) {
                                            viewModel.onBarcodeDetected(zxResult)
                                            galleryBitmap.recycle()
                                            isProcessingGallery = false
                                            return@addOnSuccessListener
                                        }
                                    }
                                    galleryBitmap.recycle()
                                }
                            } catch (e: Exception) {
                                Log.w(TAG, "ZXing gallery fallback failed", e)
                            }

                            viewModel.onParseFailed(
                                "No se encontró ningún código de barras en la imagen.\n" +
                                        "Asegúrate de que:\n" +
                                        "• La foto sea nítida y con buena iluminación\n" +
                                        "• El código PDF417 o QR sea visible completo\n" +
                                        "• No sea una captura de pantalla (pierde calidad)"
                            )
                        }
                        isProcessingGallery = false
                    }
                    .addOnFailureListener { e ->
                        viewModel.onParseFailed("Error al procesar imagen: ${e.message}")
                        isProcessingGallery = false
                    }
            } catch (e: Exception) {
                viewModel.onParseFailed("Error al leer imagen: ${e.message}")
                isProcessingGallery = false
            }
        }
    }

    DisposableEffect(lifecycleOwner, hasCameraPermission) {
        if (!hasCameraPermission) return@DisposableEffect onDispose {}

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // ── FIX 1: Resolución HD para PDF417 densos ──
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .setTargetResolution(android.util.Size(1920, 1080))
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null && state is ScannerState.Scanning) {
                            framesAnalyzed++
                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )
                            liveBarcodeScanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    val barcode = barcodes.firstOrNull()
                                    if (barcode != null) {
                                        val raw = barcode.rawValue ?: ""
                                        val bytes = barcode.rawBytes
                                        processRawBarcode(raw, bytes, "MLKit-Live")
                                    } else if (framesAnalyzed % 5 == 0) {
                                        // ── FIX 3: ZXing fallback cada 5 frames ──
                                        val bitmap = yuv420ToBitmap(imageProxy)
                                        if (bitmap != null) {
                                            val zxRaw = tryZxingDecode(bitmap)
                                            if (zxRaw != null) {
                                                processRawBarcode(zxRaw, null, "ZXing-Live")
                                            }
                                            bitmap.recycle()
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
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalyzer
                )

                // ── FIX 2: Auto-focus continuo + zoom base 1.3x ──
                camera.cameraControl.let { ctrl ->
                    ctrl.setLinearZoom(0.15f) // ~1.3x zoom
                    // Focus continuo al centro
                    val factory = SurfaceOrientedMeteringPointFactory(1f, 1f)
                    val point = factory.createPoint(0.5f, 0.5f)
                    val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                        .setAutoCancelDuration(3, java.util.concurrent.TimeUnit.SECONDS)
                        .build()
                    ctrl.startFocusAndMetering(action)
                }
            } catch (e: Exception) {
                viewModel.onError("Error al iniciar cámara: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            liveBarcodeScanner.close()
            galleryBarcodeScanner.close()
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
        ModuleTutorialLauncher(TutorialModule.SCANNER)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                !hasCameraPermission -> {
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
                        isDetected = state is ScannerState.Found,
                        isParseFailed = state is ScannerState.ParseFailed
                    )

                    // Scanning indicator
                    if (state is ScannerState.Scanning) {
                        val infiniteTransition = rememberInfiniteTransition(label = "scan_pulse")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800, easing = EaseInOut),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse_alpha"
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 8.dp, end = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Canvas(modifier = Modifier.size(10.dp)) {
                                drawCircle(
                                    color = Color.Red,
                                    alpha = alpha
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Escaneando…",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Gallery button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 110.dp)
                            .padding(horizontal = 32.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isProcessingGallery
                        ) {
                            if (isProcessingGallery) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Procesando imagen...")
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Image,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Subir desde galería")
                            }
                        }
                    }

                    // Mensaje de estado
                    val statusText = when (state) {
                        is ScannerState.Scanning    -> "Apunta al código de barras o QR de la factura\n(esquina inferior del documento)"
                        is ScannerState.Found       -> "✅ Factura detectada"
                        is ScannerState.Error       -> "⚠️ ${(state as ScannerState.Error).message}"
                        is ScannerState.ParseFailed -> "⚠️ ${(state as ScannerState.ParseFailed).reason}"
                    }
                    val statusBg = when (state) {
                        is ScannerState.ParseFailed -> Color(0xFFE65100).copy(alpha = 0.85f)
                        is ScannerState.Found       -> Color(0xFF2E7D32).copy(alpha = 0.85f)
                        else                        -> Color.Black.copy(alpha = 0.65f)
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
                            color = statusBg,
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
 * FIX 4: Frame height 25% → 35% para cubrir mejor el timbre.
 */
@Composable
private fun ScannerOverlay(
    isDetected: Boolean,
    isParseFailed: Boolean = false
) {
    val frameColor = when {
        isDetected    -> Color(0xFF4CAF50)
        isParseFailed -> Color(0xFFFF9800)
        else          -> Color.White
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth  = size.width
        val canvasHeight = size.height

        val frameWidth  = canvasWidth * 0.82f
        val frameHeight = canvasWidth * 0.35f  // FIX 4: era 0.25f
        val frameLeft   = (canvasWidth  - frameWidth)  / 2f
        val frameTop    = (canvasHeight - frameHeight) / 2f

        drawRect(color = Color.Black.copy(alpha = 0.55f))

        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(frameLeft, frameTop),
            size = Size(frameWidth, frameHeight),
            cornerRadius = CornerRadius(8.dp.toPx()),
            blendMode = BlendMode.Clear
        )

        drawRoundRect(
            color = frameColor,
            topLeft = Offset(frameLeft, frameTop),
            size = Size(frameWidth, frameHeight),
            cornerRadius = CornerRadius(8.dp.toPx()),
            style = Stroke(width = 3.dp.toPx())
        )

        val cornerLen = 24.dp.toPx()
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
