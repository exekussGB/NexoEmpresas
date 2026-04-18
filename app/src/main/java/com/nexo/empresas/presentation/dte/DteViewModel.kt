package com.nexo.empresas.presentation.dte

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexo.empresas.core.session.TenantManager
import com.nexo.empresas.dte.data.model.*
import com.nexo.empresas.dte.data.repository.DteRepository
import com.nexo.empresas.dte.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── UI States ────────────────────────────────────────────────────────────────

data class DteListUiState(
    val dtes: List<Dte> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val estadoFiltro: String? = null
)

data class EmitirDteUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val dteEmitido: Dte? = null,
    val error: String? = null,
    val tipoDte: TipoDte = TipoDte.FACTURA_ELECTRONICA,
    val rutReceptor: String = "",
    val razonSocialReceptor: String = "",
    val giroReceptor: String = "",
    val direccionReceptor: String = "",
    val items: List<ItemFormState> = listOf(ItemFormState()),
    val rutLookupLoading: Boolean = false,
    val rutLookupError: String? = null
)

data class ItemFormState(
    val descripcion: String = "",
    val cantidad: String = "1",
    val precioUnitario: String = "",
    val descuento: String = "0"
) {
    val cantidadDouble: Double get() = cantidad.toDoubleOrNull() ?: 1.0
    val precioLong: Long get() = precioUnitario.toLongOrNull() ?: 0L
    val descuentoDouble: Double get() = descuento.toDoubleOrNull() ?: 0.0
    val montoNeto: Long get() = ((cantidadDouble * precioLong) * (1 - descuentoDouble / 100)).toLong()
    val isValid: Boolean get() = descripcion.isNotBlank() && precioLong > 0
}

data class DetalleDteUiState(
    val dte: Dte? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val pdfUrl: String? = null,
    val xmlUrl: String? = null,
    val consultandoEstado: Boolean = false
)

data class FoliosUiState(
    val folios: List<Folio> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class OnboardingUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    val pfxNombreArchivo: String? = null,
    val pfxBase64: String? = null,
    val clavePfx: String = ""
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class DteViewModel @Inject constructor(
    private val repository: DteRepository,
    private val tenantManager: TenantManager
) : ViewModel() {

    // Lee el empresaId dinámicamente en cada llamada — nunca en el constructor
    private fun getEmpresaId(): String? = tenantManager.currentEmpresaId

    // ── Lista de DTEs ─────────────────────────────────────────────────────────

    private val _listaState = MutableStateFlow(DteListUiState())
    val listaState: StateFlow<DteListUiState> = _listaState.asStateFlow()

    fun cargarDtes(estadoFiltro: String? = null) {
        val empresaId = getEmpresaId()
        if (empresaId == null) {
            _listaState.update { it.copy(isLoading = false, error = "Empresa no configurada") }
            return
        }
        viewModelScope.launch {
            repository.listarDtes(empresaId, estadoFiltro)
                .collect { result ->
                    _listaState.update {
                        when (result) {
                            is Result.Loading -> it.copy(isLoading = true, error = null)
                            is Result.Success -> it.copy(
                                isLoading = false,
                                dtes = result.data,
                                estadoFiltro = estadoFiltro
                            )
                            is Result.Error -> it.copy(isLoading = false, error = result.message)
                        }
                    }
                }
        }
    }

    fun filtrarPorEstado(estado: String?) = cargarDtes(estado)

    // ── Emitir DTE ────────────────────────────────────────────────────────────

    private val _emitirState = MutableStateFlow(EmitirDteUiState())
    val emitirState: StateFlow<EmitirDteUiState> = _emitirState.asStateFlow()

    fun onTipoDteChange(tipo: TipoDte) = _emitirState.update { it.copy(tipoDte = tipo) }
    fun onRutReceptorChange(rut: String) = _emitirState.update { it.copy(rutReceptor = rut, rutLookupError = null) }
    fun onRazonSocialChange(rs: String) = _emitirState.update { it.copy(razonSocialReceptor = rs) }
    fun onGiroChange(giro: String) = _emitirState.update { it.copy(giroReceptor = giro) }
    fun onDireccionChange(dir: String) = _emitirState.update { it.copy(direccionReceptor = dir) }

    fun lookupRut(rut: String) {
        viewModelScope.launch {
            repository.lookupRut(rut).collect { result ->
                _emitirState.update {
                    when (result) {
                        is Result.Loading -> it.copy(rutLookupLoading = true, rutLookupError = null)
                        is Result.Success -> it.copy(
                            rutLookupLoading = false,
                            razonSocialReceptor = result.data.razonSocial ?: "",
                            giroReceptor = result.data.giro ?: "",
                            direccionReceptor = result.data.direccion ?: ""
                        )
                        is Result.Error -> it.copy(
                            rutLookupLoading = false,
                            rutLookupError = result.message
                        )
                    }
                }
            }
        }
    }

    fun onItemChange(index: Int, item: ItemFormState) {
        _emitirState.update { state ->
            val items = state.items.toMutableList().also { it[index] = item }
            state.copy(items = items)
        }
    }

    fun agregarItem() = _emitirState.update { it.copy(items = it.items + ItemFormState()) }

    fun eliminarItem(index: Int) {
        _emitirState.update { state ->
            if (state.items.size > 1) {
                state.copy(items = state.items.toMutableList().also { it.removeAt(index) })
            } else state
        }
    }

    fun emitirDte() {
        val empresaId = getEmpresaId()
        if (empresaId == null) {
            _emitirState.update { it.copy(error = "No hay empresa activa") }
            return
        }

        val state = _emitirState.value
        if (!validarFormulario(state)) return

        val request = EmitirDteRequest(
            empresaId = empresaId,
            tipoDte = state.tipoDte.codigo,
            rutReceptor = state.rutReceptor,
            razonSocialReceptor = state.razonSocialReceptor,
            giroReceptor = state.giroReceptor.ifBlank { null },
            direccionReceptor = state.direccionReceptor.ifBlank { null },
            items = state.items.map {
                ItemDteRequest(
                    descripcion = it.descripcion,
                    cantidad = it.cantidadDouble,
                    precioUnitario = it.precioLong,
                    descuento = it.descuentoDouble
                )
            }
        )

        viewModelScope.launch {
            repository.emitirDte(request).collect { result ->
                _emitirState.update {
                    when (result) {
                        is Result.Loading -> it.copy(isLoading = true, error = null)
                        is Result.Success -> it.copy(
                            isLoading = false,
                            success = true,
                            dteEmitido = result.data
                        )
                        is Result.Error -> it.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }

    fun resetEmitirState() = _emitirState.update { EmitirDteUiState() }

    private fun validarFormulario(state: EmitirDteUiState): Boolean {
        if (state.rutReceptor.isBlank()) {
            _emitirState.update { it.copy(error = "Ingresa el RUT del receptor") }
            return false
        }
        if (state.razonSocialReceptor.isBlank()) {
            _emitirState.update { it.copy(error = "Ingresa la razón social del receptor") }
            return false
        }
        if (state.items.any { !it.isValid }) {
            _emitirState.update { it.copy(error = "Completa todos los items correctamente") }
            return false
        }
        return true
    }

    // ── Detalle DTE ───────────────────────────────────────────────────────────

    private val _detalleState = MutableStateFlow(DetalleDteUiState())
    val detalleState: StateFlow<DetalleDteUiState> = _detalleState.asStateFlow()

    fun cargarDetalle(dteId: String) {
        viewModelScope.launch {
            repository.obtenerDte(dteId).collect { result ->
                _detalleState.update {
                    when (result) {
                        is Result.Loading -> it.copy(isLoading = true, error = null)
                        is Result.Success -> it.copy(isLoading = false, dte = result.data)
                        is Result.Error -> it.copy(isLoading = false, error = result.message)
                    }
                }
            }

            _detalleState.value.dte?.let { dte ->
                dte.pdfUrl?.let { path ->
                    runCatching { repository.getPdfUrl(path) }
                        .onSuccess { url -> _detalleState.update { it.copy(pdfUrl = url) } }
                }
                dte.xmlFirmadoUrl?.let { path ->
                    runCatching { repository.getXmlUrl(path) }
                        .onSuccess { url -> _detalleState.update { it.copy(xmlUrl = url) } }
                }
            }
        }
    }

    fun consultarEstadoSII(dteId: String) {
        viewModelScope.launch {
            _detalleState.update { it.copy(consultandoEstado = true) }
            repository.consultarEstado(dteId).collect { result ->
                when (result) {
                    is Result.Success -> _detalleState.update { s ->
                        s.copy(
                            consultandoEstado = false,
                            dte = s.dte?.copy(
                                estadoSii = result.data.estado,
                                trackId = result.data.trackId
                            )
                        )
                    }
                    is Result.Error -> _detalleState.update {
                        it.copy(consultandoEstado = false, error = result.message)
                    }
                    else -> Unit
                }
            }
        }
    }

    // ── Folios ────────────────────────────────────────────────────────────────

    private val _foliosState = MutableStateFlow(FoliosUiState())
    val foliosState: StateFlow<FoliosUiState> = _foliosState.asStateFlow()

    fun cargarFolios() {
        val empresaId = getEmpresaId()
        if (empresaId == null) {
            _foliosState.update { it.copy(error = "Empresa no configurada") }
            return
        }
        viewModelScope.launch {
            repository.listarFolios(empresaId).collect { result ->
                _foliosState.update {
                    when (result) {
                        is Result.Loading -> it.copy(isLoading = true)
                        is Result.Success -> it.copy(isLoading = false, folios = result.data)
                        is Result.Error -> it.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }

    // ── Onboarding / Certificado ──────────────────────────────────────────────

    private val _onboardingState = MutableStateFlow(OnboardingUiState())
    val onboardingState: StateFlow<OnboardingUiState> = _onboardingState.asStateFlow()

    fun onPfxSeleccionado(nombre: String, base64: String) {
        _onboardingState.update { it.copy(pfxNombreArchivo = nombre, pfxBase64 = base64) }
    }

    fun onClavePfxChange(clave: String) =
        _onboardingState.update { it.copy(clavePfx = clave) }

    fun registrarCertificado() {
        val empresaId = getEmpresaId()
        if (empresaId == null) {
            _onboardingState.update { it.copy(error = "Empresa no configurada") }
            return
        }

        val state = _onboardingState.value
        if (state.pfxBase64 == null) {
            _onboardingState.update { it.copy(error = "Selecciona el archivo .pfx") }
            return
        }
        if (state.clavePfx.isBlank()) {
            _onboardingState.update { it.copy(error = "Ingresa la clave del certificado") }
            return
        }

        viewModelScope.launch {
            repository.registrarCertificado(empresaId, state.pfxBase64, state.clavePfx)
                .collect { result ->
                    _onboardingState.update {
                        when (result) {
                            is Result.Loading -> it.copy(isLoading = true, error = null)
                            is Result.Success -> it.copy(isLoading = false, success = result.data)
                            is Result.Error -> it.copy(isLoading = false, error = result.message)
                        }
                    }
                }
        }
    }
}
