package cl.nexo.empresas.presentation.documentos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.nexo.empresas.core.session.SessionManager
import cl.nexo.empresas.data.model.*
import cl.nexo.empresas.domain.repository.ContactosRepository
import cl.nexo.empresas.domain.repository.CuentasCorrientesRepository
import cl.nexo.empresas.domain.repository.DocumentosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ChequeForm(
    val numeroCheque: String = "",
    val banco: String = "",
    val monto: String = "",
    val fechaCobro: String = LocalDate.now().plusDays(30).toString(),
    val orden: Int = 1
)

data class AddDocumentoUiState(
    val tipo: String = "egreso",
    val numeroDocumento: String = "",
    val contactoId: String? = null,
    val descripcion: String = "",
    val categoria: String = CategoriaDocumento.SERVICIOS.value,
    val monto: String = "",
    val cuentaCorrienteId: String? = null,
    val fechaMovimiento: String = LocalDate.now().toString(),
    val fechaVencimiento: String = LocalDate.now().plusDays(30).toString(),
    val metodoPago: String = MetodoPago.TRANSFERENCIA.value,
    val notas: String = "",
    val cheques: List<ChequeForm> = emptyList(),
    val contactos: List<Contacto> = emptyList(),
    val cuentas: List<CuentaCorriente> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val savedSuccessfully: Boolean = false
) {
    val montoLong: Long get() = monto.replace(".", "").replace(",", "").toLongOrNull() ?: 0L
    val sumaChequesLong: Long get() = cheques.sumOf { it.monto.replace(".", "").replace(",", "").toLongOrNull() ?: 0L }
    val chequesDiff: Long get() = montoLong - sumaChequesLong
    val isChequePago: Boolean get() = metodoPago == MetodoPago.CHEQUE.value
    val chequesValidos: Boolean get() = !isChequePago || (cheques.isNotEmpty() && chequesDiff == 0L)
}

@HiltViewModel
class AddDocumentoViewModel @Inject constructor(
    private val docRepository: DocumentosRepository,
    private val contactosRepository: ContactosRepository,
    private val cuentasRepository: CuentasCorrientesRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddDocumentoUiState())
    val uiState: StateFlow<AddDocumentoUiState> = _uiState.asStateFlow()

    init { cargarCatalogas() }

    private fun cargarCatalogas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val empresaId = sessionManager.currentEmpresaId ?: ""
            val contactos = contactosRepository.getContactos(empresaId).getOrElse { emptyList() }
            val cuentas = cuentasRepository.getCuentas(empresaId).getOrElse { emptyList() }.filter { it.activa }
            _uiState.value = _uiState.value.copy(contactos = contactos, cuentas = cuentas, isLoading = false)
        }
    }

    fun setTipo(tipo: String) { _uiState.value = _uiState.value.copy(tipo = tipo) }
    fun setNumeroDocumento(v: String) { _uiState.value = _uiState.value.copy(numeroDocumento = v) }
    fun setContactoId(v: String?) { _uiState.value = _uiState.value.copy(contactoId = v) }
    fun setDescripcion(v: String) { _uiState.value = _uiState.value.copy(descripcion = v) }
    fun setCategoria(v: String) { _uiState.value = _uiState.value.copy(categoria = v) }
    fun setMonto(v: String) { _uiState.value = _uiState.value.copy(monto = v) }
    fun setCuentaId(v: String?) { _uiState.value = _uiState.value.copy(cuentaCorrienteId = v) }
    fun setFechaMovimiento(v: String) { _uiState.value = _uiState.value.copy(fechaMovimiento = v) }
    fun setFechaVencimiento(v: String) { _uiState.value = _uiState.value.copy(fechaVencimiento = v) }
    fun setNotas(v: String) { _uiState.value = _uiState.value.copy(notas = v) }
    fun setMetodoPago(v: String) {
        val cheques = if (v == MetodoPago.CHEQUE.value && _uiState.value.cheques.isEmpty())
            listOf(ChequeForm()) else _uiState.value.cheques
        _uiState.value = _uiState.value.copy(metodoPago = v, cheques = cheques)
    }
    fun addCheque() {
        val nuevo = ChequeForm(orden = _uiState.value.cheques.size + 1)
        _uiState.value = _uiState.value.copy(cheques = _uiState.value.cheques + nuevo)
    }
    fun removeCheque(index: Int) {
        _uiState.value = _uiState.value.copy(cheques = _uiState.value.cheques.toMutableList().also { it.removeAt(index) })
    }
    fun updateCheque(index: Int, cheque: ChequeForm) {
        val list = _uiState.value.cheques.toMutableList()
        list[index] = cheque
        _uiState.value = _uiState.value.copy(cheques = list)
    }

    fun guardar() {
        val state = _uiState.value
        if (state.descripcion.isBlank()) {
            _uiState.value = state.copy(error = "La descripción es obligatoria")
            return
        }
        if (state.montoLong <= 0) {
            _uiState.value = state.copy(error = "El monto debe ser mayor a 0")
            return
        }
        if (state.isChequePago && !state.chequesValidos) {
            _uiState.value = state.copy(error = "La suma de cheques debe igualar el monto del documento")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            val empresaId = sessionManager.currentEmpresaId ?: ""
            val userId = sessionManager.currentUserId ?: ""

            val doc = DocumentoCreate(
                empresaId = empresaId,
                tipo = state.tipo,
                numeroDocumento = state.numeroDocumento.takeIf { it.isNotBlank() },
                contactoId = state.contactoId,
                descripcion = state.descripcion,
                categoria = state.categoria.takeIf { it.isNotBlank() },
                monto = state.montoLong,
                cuentaCorrienteId = state.cuentaCorrienteId,
                fechaMovimiento = state.fechaMovimiento,
                fechaVencimiento = state.fechaVencimiento,
                metodoPago = state.metodoPago,
                notas = state.notas.takeIf { it.isNotBlank() },
                createdBy = userId
            )

            val cheques = if (state.isChequePago) {
                state.cheques.mapIndexed { i, c ->
                    ChequeCreate(
                        documentoId = "", // será reemplazado en el repositorio
                        empresaId = empresaId,
                        numeroCheque = c.numeroCheque,
                        banco = c.banco.takeIf { it.isNotBlank() },
                        monto = c.monto.replace(".", "").replace(",", "").toLongOrNull() ?: 0L,
                        fechaCobro = c.fechaCobro,
                        orden = i + 1
                    )
                }
            } else emptyList()

            docRepository.addDocumento(doc, cheques)
                .onSuccess { _uiState.value = _uiState.value.copy(isSaving = false, savedSuccessfully = true) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isSaving = false, error = e.message) }
        }
    }
}
