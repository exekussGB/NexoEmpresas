package cl.nexo.empresas.presentation.graficos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.nexo.empresas.core.session.SessionManager
import cl.nexo.empresas.core.session.TenantManager
import cl.nexo.empresas.data.model.DocumentoDetalle
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

        // ═══════════════════════════════════════════════════════════════════
        // Sección 1: Cobrado vs Pagado (resumen mensual)
        // ═══════════════════════════════════════════════════════════════════
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

        // ═══════════════════════════════════════════════════════════════════
        // Sección 2: Saldo por cuenta
        // ═══════════════════════════════════════════════════════════════════
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

        // ═══════════════════════════════════════════════════════════════════
        // Sección 3: DETALLE DE MOVIMIENTOS (NUEVO)
        // ═══════════════════════════════════════════════════════════════════
        if (data.detalle.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("=== Detalle de Movimientos (${_meses.value} meses) ===")
            sb.appendLine("Fecha,Tipo,Descripcion,Monto,Metodo Pago,N° Documento,Categoria,Estado,Contacto,Cuenta,Fecha Pago,Fecha Vencimiento,Notas,Cheques")

            data.detalle.forEach { doc ->
                val chequesStr = formatCheques(doc)
                sb.appendLine(buildCsvRow(doc, chequesStr))
            }

            // Subtotales por tipo
            sb.appendLine()
            sb.appendLine("=== Subtotales por Tipo ===")
            val ingresos = data.detalle.filter { it.tipo == "ingreso" }
            val egresos  = data.detalle.filter { it.tipo == "egreso" }
            sb.appendLine("Total Ingresos (${ingresos.size} movimientos),${ingresos.sumOf { it.monto }}")
            sb.appendLine("Total Egresos (${egresos.size} movimientos),${egresos.sumOf { it.monto }}")
            sb.appendLine("Saldo,${ingresos.sumOf { it.monto } - egresos.sumOf { it.monto }}")

            // Subtotales por método de pago
            sb.appendLine()
            sb.appendLine("=== Subtotales por Método de Pago ===")
            sb.appendLine("Metodo,Cantidad,Monto Total")
            data.detalle
                .groupBy { it.metodoPago }
                .forEach { (metodo, docs) ->
                    val label = when (metodo) {
                        "transferencia" -> "Transferencia"
                        "cheque"        -> "Cheque"
                        "efectivo"      -> "Efectivo"
                        else            -> "Otro"
                    }
                    sb.appendLine("$label,${docs.size},${docs.sumOf { it.monto }}")
                }

            // Subtotales por categoría
            sb.appendLine()
            sb.appendLine("=== Subtotales por Categoría ===")
            sb.appendLine("Categoria,Cantidad,Monto Total")
            data.detalle
                .groupBy { it.categoria.ifBlank { "sin_categoria" } }
                .forEach { (cat, docs) ->
                    val label = when (cat) {
                        "honorarios"    -> "Honorarios"
                        "servicios"     -> "Servicios"
                        "materiales"    -> "Materiales"
                        "arriendo"      -> "Arriendo"
                        "otro"          -> "Otro"
                        else            -> "Sin categoría"
                    }
                    sb.appendLine("$label,${docs.size},${docs.sumOf { it.monto }}")
                }
        }

        return sb.toString()
    }

    /**
     * Formatea los cheques asociados a un documento en un string legible.
     * Ejemplo: "Cheque #001 Banco Chile $500.000 (cobrado) | Cheque #002 Banco Estado $1.000.000 (pendiente)"
     */
    private fun formatCheques(doc: DocumentoDetalle): String {
        if (doc.cheques.isEmpty()) return ""
        return doc.cheques.joinToString(" | ") { ch ->
            val banco = if (ch.banco.isNotBlank()) " ${ch.banco}" else ""
            "Cheque #${ch.numeroCheque}$banco \$${formatMonto(ch.monto)} (${ch.estado})"
        }
    }

    /**
     * Construye una fila CSV escapando correctamente los campos que puedan contener comas.
     */
    private fun buildCsvRow(doc: DocumentoDetalle, chequesStr: String): String {
        val fields = listOf(
            doc.fechaMovimiento,
            if (doc.tipo == "ingreso") "Ingreso" else "Egreso",
            doc.descripcion,
            doc.monto.toString(),
            when (doc.metodoPago) {
                "transferencia" -> "Transferencia"
                "cheque"        -> "Cheque"
                "efectivo"      -> "Efectivo"
                else            -> "Otro"
            },
            doc.numeroDocumento,
            when (doc.categoria) {
                "honorarios" -> "Honorarios"
                "servicios"  -> "Servicios"
                "materiales" -> "Materiales"
                "arriendo"   -> "Arriendo"
                "otro"       -> "Otro"
                else         -> ""
            },
            when (doc.estado) {
                "pendiente" -> "Pendiente"
                "pagado"    -> "Pagado"
                "anulado"   -> "Anulado"
                else        -> doc.estado
            },
            doc.contactoNombre,
            doc.cuentaNombre,
            doc.fechaPago ?: "",
            doc.fechaVencimiento ?: "",
            doc.notas,
            chequesStr
        )
        // Escapar campos que contengan comas, comillas o saltos de línea
        return fields.joinToString(",") { field ->
            if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
                "\"${field.replace("\"", "\"\"")}\""
            } else {
                field
            }
        }
    }

    /** Formatea montos con separador de miles: 1500000 → 1.500.000 */
    private fun formatMonto(monto: Long): String {
        return monto.toString().reversed().chunked(3).joinToString(".").reversed()
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
