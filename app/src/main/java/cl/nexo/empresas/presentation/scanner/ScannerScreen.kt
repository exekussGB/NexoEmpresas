package cl.nexo.empresas.presentation.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.net.Uri
import android.util.Size
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import cl.nexo.empresas.core.tutorial.TutorialModule
import cl.nexo.empresas.data.model.DteScanResult
import cl.nexo.empresas.presentation.tutorial.ModuleTutorialLauncher
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.pdf417.PDF417Reader
import java.io.ByteArrayOutputStream

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

    var isProcessingGallery by remember { mutableStateOf(false) }
    var framesAnalyzed by remember { mutableIntStateOf(0) }

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

    // ── Auto-reset tras ParseFailed ─────────────────────────────────────────
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

    // Scanner ML Kit para cámara: PDF417 + QR
    val liveBarcodeScanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_PDF417, Barcode.FORMAT_QR_CODE)
                .build()
        )
    }

    // Scanner ML Kit para galería: todos los formatos
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

    // ── ZXing PDF417 fallback reader ────────────────────────────────────────
    val zxingPdf417Reader = remember { PDF417Reader() }

    /**
     * Intenta decodificar PDF417 usando ZXing como fallback cuando ML Kit falla.
     * ZXing es significativamente mejor con PDF417 densos como los timbres DTE.
     */
    fun tryZxingDecode(bitmap: Bitmap): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val hints = mapOf(
                DecodeHintType.TRY_HARDER to true,
                DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.PDF_417)
            )
            val result = zxingPdf417Reader.decode(binaryBitmap, hints)
            result.text
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Convierte un frame YUV de CameraX a Bitmap para ZXing.
     */
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
            val bytes = out.toByteArray()
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Procesa el resultado de un escaneo exitoso (ML Kit o ZXing).
     */
    fun processRawBarcode(raw: String, rawBytes: ByteArray? = null, source: String = "PDF417"): Boolean {
        // Intentar primero con rawValue (String)
        var result = TedParser.parse(raw)
        // Si falla, intentar con rawBytes y múltiples encodings
        if (result == null && rawBytes != null) {
            result = TedParser.parseFromBytes(rawBytes)
        }
        return if (result != null) {
            viewModel.onBarcodeDetected(result)
            true
        } else {
            val reason = TedParser.diagnose(raw)
            viewModel.onParseFailed(reason)
            false
        }
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
                                // Intentar primero con rawValue (String)
        var result = TedParser.parse(raw)
        // Si falla, intentar con rawBytes y múltiples encodings
        if (result == null && rawBytes != null) {
            result = TedParser.parseFromBytes(rawBytes)
        }
                                if (result != null) {
                                    viewModel.onBarcodeDetected(result)
                                    parsed = true
                                    break
                                }
                            }

                            if (!parsed) {
                                val firstBarcode = sorted.first()
                                val firstRaw = firstBarcode.rawValue ?: ""
                                // Último intento con rawBytes del primer barcode
                                val bytesResult = TedParser.parseFromBytes(firstBarcode.rawBytes)
                                if (bytesResult != null) {
                                    viewModel.onBarcodeDetected(bytesResult)
                                    isProcessingGallery = false
                                    return@addOnSuccessListener
                                }
                                val formatName = when (sorted.first().format) {
                                    Barcode.FORMAT_PDF417     -> "PDF417"
                                    Barcode.FORMAT_QR_CODE    -> "QR"
                                    Barcode.FORMAT_DATA_MATRIX -> "DataMatrix"
                                    Barcode.FORMAT_CODE_128   -> "Code128"
                                    Barcode.FORMAT_EAN_13     -> "EAN-13"
                                    else                       -> "código"
                                }
                                val reason = TedParser.diagnose(firstRaw)
                                viewModel.onParseFailed("Se detectó un $formatName pero no es una factura DTE.\n$reason")
                            }
                            isProcessingGallery = false
                        } else {
                            // ML Kit no detectó nada → fallback a ZXing
                            tryZxingGalleryFallback(context, uri, viewModel) {
                                isProcessingGallery = false
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        // ML Kit falló → intentar ZXing
                        tryZxingGalleryFallback(context, uri, viewModel) {
                            isProcessingGallery = false
                        }
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

            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            // ── FIX 1: Alta resolución para PDF417 densos ───────────────
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(Size(1920, 1080))
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
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
                                        processRawBarcode(raw, bytes)
                                    } else if (framesAnalyzed % 5 == 0) {
                                        // ── FIX 3: ZXing fallback cada 5 frames ─────
                                        val bitmap = yuv420ToBitmap(imageProxy)
                                        if (bitmap != null) {
                                            val raw = tryZxingDecode(bitmap)
                                            if (raw != null) {
                                                processRawBarcode(raw, "ZXing-PDF417")
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

                // ── FIX 2: Auto-focus continuo + zoom base ──────────────
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalyzer
                )

                // Activar auto-focus continuo
                camera.cameraControl.cancelFocusAndMetering()
                val factory = SurfaceOrientedMeteringPointFactory(1f, 1f)
                val centerPoint = factory.createPoint(0.5f, 0.5f)
                val action = FocusMeteringAction.Builder(centerPoint, FocusMeteringAction.FLAG_AF)
                    .setAutoCancelDuration(3, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                camera.cameraControl.startFocusAndMetering(action)

                // Zoom base 1.3x para acercarse más al PDF417
                camera.cameraControl.setLinearZoom(0.15f)

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
                        Text("📷", style = MaterialTheme.typography.displayMedium)
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
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier.fillMaxSize()
                    )

                    ScannerOverlay(
                        isDetected = state is ScannerState.Found,
                        isParseFailed = state is ScannerState.ParseFailed
                    )

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
                                drawCircle(color = Color.Red, alpha = alpha)
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

                    // Status message
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
 * Fallback: si ML Kit no detecta nada en una imagen de galería,
 * se usa ZXing PDF417Reader que es más robusto con timbres DTE densos.
 */
private fun tryZxingGalleryFallback(
    context: android.content.Context,
    uri: Uri,
    viewModel: ScannerViewModel,
    onComplete: () -> Unit
) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: run {
            viewModel.onParseFailed(
                "No se encontró ningún código de barras en la imagen.\n" +
                        "Asegúrate de que:\n" +
                        "• La foto sea nítida y con buena iluminación\n" +
                        "• El código PDF417 o QR sea visible completo\n" +
                        "• No sea una captura de pantalla (pierde calidad)"
            )
            onComplete()
            return
        }

        // Decodificar a múltiples resoluciones para mejorar detección
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        if (originalBitmap == null) {
            viewModel.onParseFailed("Error al decodificar la imagen.")
            onComplete()
            return
        }

        // Intentar con la imagen original y con versiones escaladas
        val bitmapsToTry = mutableListOf(originalBitmap)

        // Si la imagen es muy grande, probar también con versión reducida
        if (originalBitmap.width > 2000 || originalBitmap.height > 2000) {
            val scale = 1500f / maxOf(originalBitmap.width, originalBitmap.height)
            val scaled = Bitmap.createScaledBitmap(
                originalBitmap,
                (originalBitmap.width * scale).toInt(),
                (originalBitmap.height * scale).toInt(),
                true
            )
            bitmapsToTry.add(scaled)
        }

        // Si es muy pequeña, probar con versión ampliada
        if (originalBitmap.width < 800 || originalBitmap.height < 800) {
            val scale = 1200f / minOf(originalBitmap.width, originalBitmap.height)
            val scaled = Bitmap.createScaledBitmap(
                originalBitmap,
                (originalBitmap.width * scale).toInt(),
                (originalBitmap.height * scale).toInt(),
                true
            )
            bitmapsToTry.add(scaled)
        }

        val reader = PDF417Reader()
        val hints = mapOf(
            DecodeHintType.TRY_HARDER to true,
            DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.PDF_417)
        )

        var decoded = false
        for (bitmap in bitmapsToTry) {
            try {
                val width = bitmap.width
                val height = bitmap.height
                val pixels = IntArray(width * height)
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                val source = RGBLuminanceSource(width, height, pixels)
                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                val zxResult = reader.decode(binaryBitmap, hints)

                val raw = zxResult.text
                // Intentar primero con rawValue (String)
        var result = TedParser.parse(raw)
        // Si falla, intentar con rawBytes y múltiples encodings
        if (result == null && rawBytes != null) {
            result = TedParser.parseFromBytes(rawBytes)
        }
                if (result != null) {
                    viewModel.onBarcodeDetected(result)
                    decoded = true
                    break
                } else {
                    val reason = TedParser.diagnose(raw)
                    viewModel.onParseFailed("Se detectó un PDF417 pero no es una factura DTE.\n$reason")
                    decoded = true
                    break
                }
            } catch (_: Exception) {
                // This bitmap size didn't work, try next
            }
        }

        if (!decoded) {
            viewModel.onParseFailed(
                "No se encontró ningún código de barras en la imagen.\n" +
                        "Asegúrate de que:\n" +
                        "• La foto sea nítida y con buena iluminación\n" +
                        "• El código PDF417 o QR sea visible completo\n" +
                        "• No sea una captura de pantalla (pierde calidad)"
            )
        }

        // Limpiar bitmaps
        bitmapsToTry.forEach { if (it != originalBitmap) it.recycle() }
        originalBitmap.recycle()
    } catch (e: Exception) {
        viewModel.onParseFailed("Error al procesar imagen: ${e.message}")
    }
    onComplete()
}

/**
 * Overlay con máscara oscura + marco guía centrado para el PDF417.
 * Marco más alto para acomodar timbres DTE completos.
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

        // Marco más grande para PDF417 densos
        val frameWidth  = canvasWidth * 0.85f
        val frameHeight = canvasWidth * 0.35f  // Más alto que antes (0.25f)
        val frameLeft   = (canvasWidth  - frameWidth)  / 2f
        val frameTop    = (canvasHeight - frameHeight) / 2f

        drawRect(color = Color.Black.copy(alpha = 0.55f))

        drawRoundRect(
            color = Color.Transparent,
            topLeft = androidx.compose.ui.geometry.Offset(frameLeft, frameTop),
            size = androidx.compose.ui.geometry.Size(frameWidth, frameHeight),
            cornerRadius = CornerRadius(8.dp.toPx()),
            blendMode = BlendMode.Clear
        )

        drawRoundRect(
            color = frameColor,
            topLeft = androidx.compose.ui.geometry.Offset(frameLeft, frameTop),
            size = androidx.compose.ui.geometry.Size(frameWidth, frameHeight),
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
