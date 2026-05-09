package com.nexo.empresas.presentation.dte.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexo.empresas.core.util.Constants
import com.nexo.empresas.data.model.DteScanResult
import com.nexo.empresas.presentation.scanner.TedParser
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

@HiltViewModel
class ScannerVerificacionViewModel @Inject constructor(
    private val httpClient: HttpClient
) : ViewModel() {

    private val _state = MutableStateFlow<ScannerVerificacionState>(ScannerVerificacionState.Scanning)
    val state: StateFlow<ScannerVerificacionState> = _state.asStateFlow()

    fun onBarcodeDetected(rawValue: String) {
        if (_state.value !is ScannerVerificacionState.Scanning) return

        viewModelScope.launch {
            _state.value = ScannerVerificacionState.Procesando
            val scanResult = TedParser.parse(rawValue)

            if (scanResult != null) {
                verifyWithSii(scanResult)
            } else {
                _state.value = ScannerVerificacionState.Error("No se pudo reconocer un timbre DTE válido en el código escaneado.")
            }
        }
    }

    private suspend fun verifyWithSii(scanResult: DteScanResult) {
        try {
            val response = httpClient.post("${Constants.SUPABASE_URL}/functions/v1/lookup-dte-estado") {
                header("Authorization", "Bearer ${Constants.SUPABASE_ANON_KEY}")
                contentType(ContentType.Application.Json)
                setBody(
                    LookupRequest(
                        rutEmisor = scanResult.rutEmisor,
                        tipoDte = scanResult.tipoDocumento,
                        folio = scanResult.folio.toIntOrNull() ?: 0,
                        fechaEmision = scanResult.fechaEmision,
                        montoTotal = scanResult.montoTotal
                    )
                )
            }

            if (response.status.value in 200..299) {
                val lookupResponse = response.body<LookupResponse>()
                _state.value = ScannerVerificacionState.Resultado(scanResult, lookupResponse.estado)
            } else {
                _state.value = ScannerVerificacionState.Resultado(scanResult, "No verificado (Error API)")
            }
        } catch (e: Exception) {
            _state.value = ScannerVerificacionState.Resultado(scanResult, "No verificado (${e.localizedMessage})")
        }
    }

    fun retry() {
        _state.value = ScannerVerificacionState.Scanning
    }
}

sealed class ScannerVerificacionState {
    object Scanning : ScannerVerificacionState()
    object Procesando : ScannerVerificacionState()
    data class Resultado(val result: DteScanResult, val estadoSii: String?) : ScannerVerificacionState()
    data class Error(val mensaje: String) : ScannerVerificacionState()
}

@Serializable
data class LookupRequest(
    @SerialName("rut_emisor") val rutEmisor: String,
    @SerialName("tipo_dte") val tipoDte: Int,
    val folio: Int,
    @SerialName("fecha_emision") val fechaEmision: String,
    @SerialName("monto_total") val montoTotal: Long
)

@Serializable
data class LookupResponse(
    val estado: String
)
