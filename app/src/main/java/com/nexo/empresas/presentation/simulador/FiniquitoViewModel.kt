package com.nexo.empresas.presentation.simulador

import androidx.lifecycle.ViewModel
import com.nexo.empresas.data.model.RemuneracionesChile as RC
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class FiniquitoViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(FiniquitoState())
    val state: StateFlow<FiniquitoState> = _state.asStateFlow()

    private val _result = MutableStateFlow<FiniquitoResult?>(null)
    val result: StateFlow<FiniquitoResult?> = _result.asStateFlow()

    fun updateState(transform: FiniquitoState.() -> FiniquitoState) {
        _state.value = _state.value.transform()
        calculate()
    }

    private fun calculate() {
        val s = _state.value
        if (s.sueldoBase <= 0) {
            _result.value = null
            return
        }

        val mesesTotal = ChronoUnit.MONTHS.between(s.fechaInicio, s.fechaTermino)
        val diasTotal = ChronoUnit.DAYS.between(s.fechaInicio, s.fechaTermino)

        // 1. Indemnización por años de servicio (solo Art 161)
        var añosServicioMonto = 0L
        var añosCalculados = 0
        if (s.causal == CausalFiniquito.ART_161) {
            añosCalculados = (mesesTotal / 12).toInt().coerceAtLeast(1)
            añosServicioMonto = s.sueldoBase * min(añosCalculados, 11)
        }

        // 2. Aviso previo (solo Art 161 si no se dio aviso)
        val avisoPrevioMonto = if (s.causal == CausalFiniquito.ART_161 && !s.avisoDado) {
            s.sueldoBase
        } else 0L

        // 3. Feriado Proporcional (1.25 días por mes trabajado)
        // Usamos una aproximación simple: (días totales / 365) * 15 días legales
        val diasFeriadoPendientes = (diasTotal.toDouble() / 365.0 * 15.0)
        val feriadoMonto = (s.sueldoBase / 30.0 * diasFeriadoPendientes).toLong()

        // 4. Gratificación Proporcional
        // Proporción de meses en el año actual (hasta la fecha de término)
        val mesesEnAñoActual = s.fechaTermino.monthValue
        val topeGratAnual = (RC.TOPE_GRATIFICACION_ART50 * RC.INGRESO_MINIMO).toLong()
        val gratificacionProporcional = min(
            (s.sueldoBase * 0.25 * mesesEnAñoActual).toLong(),
            (topeGratAnual / 12 * mesesEnAñoActual)
        )

        val costoTotal = añosServicioMonto + avisoPrevioMonto + feriadoMonto + gratificacionProporcional

        _result.value = FiniquitoResult(
            añosServicio = añosServicioMonto,
            añosCalculados = añosCalculados,
            avisoPrevio = avisoPrevioMonto,
            feriadoProporcional = feriadoMonto,
            diasFeriado = diasFeriadoPendientes,
            gratificacionProporcional = gratificacionProporcional,
            costoTotal = costoTotal
        )
    }
}

data class FiniquitoState(
    val sueldoBase: Long = 0,
    val fechaInicio: LocalDate = LocalDate.now().minusYears(1),
    val fechaTermino: LocalDate = LocalDate.now(),
    val tipoContrato: TipoContratoFiniquito = TipoContratoFiniquito.INDEFINIDO,
    val causal: CausalFiniquito = CausalFiniquito.ART_161,
    val avisoDado: Boolean = false
)

enum class TipoContratoFiniquito(val label: String) {
    INDEFINIDO("Indefinido"),
    PLAZO_FIJO("Plazo Fijo"),
    OBRA_FAENA("Obra o Faena")
}

enum class CausalFiniquito(val label: String, val description: String) {
    ART_159_4("Art. 159 N°4", "Vencimiento del plazo"),
    ART_159_5("Art. 159 N°5", "Conclusión de obra"),
    ART_161("Art. 161", "Necesidades de la empresa"),
    ART_160("Art. 160", "Falta grave (sin indemnización)")
}

data class FiniquitoResult(
    val añosServicio: Long,
    val añosCalculados: Int,
    val avisoPrevio: Long,
    val feriadoProporcional: Long,
    val diasFeriado: Double,
    val gratificacionProporcional: Long,
    val costoTotal: Long
)
