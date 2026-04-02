package cl.nexo.empresas.presentation.scanner

import androidx.lifecycle.ViewModel
import cl.nexo.empresas.data.model.DteScanResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed class ScannerState {
    object Scanning : ScannerState()
    data class Found(val result: DteScanResult) : ScannerState()
    data class Error(val message: String) : ScannerState()
    /** Código detectado pero no es un DTE válido */
    data class ParseFailed(val reason: String) : ScannerState()
}

@HiltViewModel
class ScannerViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow<ScannerState>(ScannerState.Scanning)
    val state: StateFlow<ScannerState> = _state.asStateFlow()

    /** Llamado por el analyzer de ML Kit cuando detecta un código válido */
    fun onBarcodeDetected(result: DteScanResult) {
        if (_state.value is ScannerState.Found) return // evitar doble disparo
        _state.value = ScannerState.Found(result)
    }

    /** Código PDF417 detectado pero no se pudo parsear como DTE */
    fun onParseFailed(reason: String) {
        // Solo mostrar si aún estamos escaneando (no sobreescribir Found)
        if (_state.value is ScannerState.Scanning) {
            _state.value = ScannerState.ParseFailed(reason)
        }
    }

    fun onError(message: String) {
        _state.value = ScannerState.Error(message)
    }

    fun reset() {
        _state.value = ScannerState.Scanning
    }
}
