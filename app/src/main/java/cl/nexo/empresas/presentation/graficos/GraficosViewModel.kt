package cl.nexo.empresas.presentation.graficos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.nexo.empresas.core.session.SessionManager
import cl.nexo.empresas.core.session.TenantManager
import cl.nexo.empresas.data.model.GraficoData
import cl.nexo.empresas.domain.repository.GraficosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GraficosViewModel @Inject constructor(
    private val repo: GraficosRepository,
    private val tenantManager: TenantManager,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<GraficosUiState>(GraficosUiState.Loading)
    val uiState: StateFlow<GraficosUiState> = _uiState

    private val _meses = MutableStateFlow(6)
    val meses: StateFlow<Int> = _meses

    init { load() }

    fun setMeses(m: Int) {
        _meses.value = m
        load()
    }

    fun load() {
        val empresaId = tenantManager.empresa?.id?.takeIf { it.isNotBlank() }
            ?: sessionManager.currentEmpresaId?.takeIf { it.isNotBlank() }
            ?: run {
                _uiState.value = GraficosUiState.Error("No hay empresa activa. Vuelve al menú principal.")
                return
            }

        viewModelScope.launch {
            _uiState.value = GraficosUiState.Loading
            repo.getGraficoData(empresaId, _meses.value)
                .onSuccess { _uiState.value = GraficosUiState.Success(it) }
                .onFailure { _uiState.value = GraficosUiState.Error(it.message ?: "Error al cargar gráficos") }
        }
    }

    // ── Generar contenido CSV ────────────────────────────────────────────
    fun generateCsvContent(): String {
        val state = _uiState.value
        if (state !is GraficosUiState.Success) return ""

        val data = state.data
        val sb = StringBuilder()

        // Sección 1: Cobrado vs Pagado
        sb.appendLine("=== Cobrado vs Pagado (${_meses.value} meses) ===")
        sb.appendLine("Mes,Cobrado,Pagado,Diferencia")
        data.mensual.forEach { item ->
            val mesLabel = formatMesLabel(item.mes)
            val diff = item.totalCobrado - item.totalPagado
            sb.appendLine("$mesLabel,${item.totalCobrado},${item.totalPagado},$diff")
        }

        // Totales
        val totalCobrado = data.mensual.sumOf { it.totalCobrado }
        val totalPagado  = data.mensual.sumOf { it.totalPagado }
        sb.appendLine("TOTAL,$totalCobrado,$totalPagado,${totalCobrado - totalPagado}")
        sb.appendLine()

        // Sección 2: Saldo por cuenta
        val cuentas = data.porCuenta.map { it.cuentaNombre }.distinct()
        if (cuentas.isNotEmpty()) {
            sb.appendLine("=== Saldo Neto por Cuenta ===")
            sb.appendLine("Cuenta,Mes,Saldo Neto")
            data.porCuenta.forEach { item ->
                val mesLabel = formatMesLabel(item.mes)
                sb.appendLine("${item.cuentaNombre},$mesLabel,${item.saldoNeto}")
            }

            // Totales por cuenta
            sb.appendLine()
            sb.appendLine("=== Totales por Cuenta ===")
            sb.appendLine("Cuenta,Saldo Neto Total")
            cuentas.forEach { nombre ->
                val total = data.porCuenta.filter { it.cuentaNombre == nombre }.sumOf { it.saldoNeto }
                sb.appendLine("$nombre,$total")
            }
        }

        return sb.toString()
    }

    private val MESES = listOf("Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic")

    private fun formatMesLabel(mes: String): String {
        // mes viene como "YYYY-MM"
        return try {
            val parts = mes.split("-")
            val year = parts[0]
            val monthIdx = parts[1].toInt() - 1
            "${MESES.getOrElse(monthIdx) { parts[1] }} $year"
        } catch (e: Exception) {
            mes
        }
    }
}

sealed class GraficosUiState {
    data object Loading : GraficosUiState()
    data class Success(val data: GraficoData) : GraficosUiState()
    data class Error(val message: String) : GraficosUiState()
}
