package com.nexo.empresas.presentation.simulador

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.min
import kotlin.math.roundToLong

// ──────────────────────────────────────────────────────────────────────────────
// CONSTANTES LEGALES (actualizar cuando cambie IMM o UF)
// Fuente: Código del Trabajo Arts. 50, 163, 172
// ──────────────────────────────────────────────────────────────────────────────
object ConstantesLaborales {
    /** Ingreso Mínimo Mensual vigente (actualizar según decreto) */
    const val IMM: Long = 539_000L          // Desde enero 2026

    /** Tope remuneración base para IAS e indemnización aviso previo (Art. 172) */
    const val TOPE_UF_90_PESOS: Long = 4_860_000L  // Aprox. 90 UF — actualizar mensualmente

    /** Tope años de servicio para IAS legal (Art. 163) */
    const val TOPE_ANIOS_IAS: Int = 11

    /** Tope anual gratificación Art. 50: 4,75 × IMM */
    val TOPE_ANUAL_GRATIFICACION: Long get() = (IMM * 4.75).roundToLong()

    /** Tope mensual gratificación Art. 50 */
    val TOPE_MENSUAL_GRATIFICACION: Long get() = (TOPE_ANUAL_GRATIFICACION / 12.0).roundToLong()

    /** Días hábiles de vacaciones legales al año (Art. 67) */
    const val DIAS_VACACIONES_ANUALES: Double = 15.0

    /** Días de vacaciones proporcionales por mes trabajado */
    const val DIAS_VACACIONES_POR_MES: Double = DIAS_VACACIONES_ANUALES / 12.0 // = 1.25
}

// ──────────────────────────────────────────────────────────────────────────────
// STATE
// ──────────────────────────────────────────────────────────────────────────────
data class FiniquitoState(
    // Datos básicos
    val sueldoBase: Long = 0L,
    val fechaInicio: LocalDate = LocalDate.now().minusYears(1),
    val fechaTermino: LocalDate = LocalDate.now(),
    val tipoContrato: TipoContratoFiniquito = TipoContratoFiniquito.INDEFINIDO,
    val causal: CausalFiniquito = CausalFiniquito.ART_161,

    // Aviso previo (solo aplica en Art. 161)
    val avisoDado: Boolean = false,

    // Vacaciones
    /** ¿El trabajador tiene vacaciones pendientes sin tomar? */
    val tieneVacacionesPendientes: Boolean = false,
    /** Días hábiles de vacaciones ya tomadas desde el último período */
    val diasVacacionesTomadas: Int = 0,

    // Remuneración variable
    val tieneRemuneracionVariable: Boolean = false,
    /** Promedio últimos 3 meses (se usa como base en vez de sueldoBase si es variable) */
    val promedioUltimos3Meses: Long = 0L,

    // Gratificación
    /** Si la empresa ya pagó gratificación mensual durante el año (no se adeuda en finiquito) */
    val gratificacionYaPagadaMensual: Boolean = true
)

// ──────────────────────────────────────────────────────────────────────────────
// ENUMS
// ──────────────────────────────────────────────────────────────────────────────
enum class TipoContratoFiniquito(val label: String) {
    INDEFINIDO("Indefinido"),
    PLAZO_FIJO("Plazo Fijo"),
    OBRA_FAENA("Obra o Faena")
}

enum class CausalFiniquito(
    val label: String,
    val description: String,
    /** ¿Genera indemnización por años de servicio? */
    val generaIAS: Boolean,
    /** ¿Genera indemnización sustitutiva de aviso previo? */
    val generaAvisoPrevio: Boolean
) {
    /** Mutuo acuerdo (Art. 159 N°1) — no hay indemnización obligatoria */
    ART_159_1(
        label = "Art. 159 N°1",
        description = "Mutuo acuerdo",
        generaIAS = false,
        generaAvisoPrevio = false
    ),
    /** Renuncia voluntaria (Art. 159 N°2) — no hay indemnización */
    ART_159_2(
        label = "Art. 159 N°2",
        description = "Renuncia voluntaria",
        generaIAS = false,
        generaAvisoPrevio = false
    ),
    /** Vencimiento del plazo (Art. 159 N°4) — no hay indemnización por años */
    ART_159_4(
        label = "Art. 159 N°4",
        description = "Vencimiento del plazo",
        generaIAS = false,
        generaAvisoPrevio = false
    ),
    /** Conclusión de obra o faena (Art. 159 N°5) */
    ART_159_5(
        label = "Art. 159 N°5",
        description = "Conclusión de obra o faena",
        generaIAS = false,
        generaAvisoPrevio = false
    ),
    /** Necesidades de la empresa (Art. 161) — la principal causal con IAS */
    ART_161(
        label = "Art. 161",
        description = "Necesidades de la empresa",
        generaIAS = true,
        generaAvisoPrevio = true
    ),
    /** Falta grave (Art. 160) — sin indemnización */
    ART_160(
        label = "Art. 160",
        description = "Falta grave (sin indemnización)",
        generaIAS = false,
        generaAvisoPrevio = false
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// RESULT
// ──────────────────────────────────────────────────────────────────────────────
data class FiniquitoResult(
    // Remuneración proporcional del último mes
    val remuneracionUltimoMes: Long,
    val diasUltimoMes: Int,

    // Indemnización por años de servicio (Art. 163)
    val indemnizacionAnios: Long,
    val aniosReconocidos: Int,
    val baseCalculoIAS: Long,          // Puede estar topada a 90 UF
    val topadaPor90UF: Boolean,

    // Indemnización sustitutiva aviso previo (Art. 162)
    val indemnizacionAvisoPrevio: Long,

    // Feriado proporcional (Art. 67)
    val feriadoProporcional: Long,
    val diasFeriadoBruto: Double,       // Días acumulados antes de restar tomados
    val diasFeriadoNeto: Double,        // Días a pagar

    // Gratificación proporcional (Art. 50)
    val gratificacionProporcional: Long,
    val mesesGratificacion: Int,

    // Total
    val costoTotal: Long
)

// ──────────────────────────────────────────────────────────────────────────────
// VIEW MODEL
// ──────────────────────────────────────────────────────────────────────────────
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

    fun reset() {
        _state.value = FiniquitoState()
        _result.value = null
    }

    private fun calculate() {
        val s = _state.value

        // Validación mínima
        if (s.sueldoBase <= 0) {
            _result.value = null
            return
        }
        if (!s.fechaTermino.isAfter(s.fechaInicio)) {
            _result.value = null
            return
        }

        // ── Base de cálculo ──────────────────────────────────────────────────
        // Art. 172: si hay remuneración variable usar promedio de los últimos 3 meses
        val baseRemuneracion: Long = if (s.tieneRemuneracionVariable && s.promedioUltimos3Meses > 0) {
            s.promedioUltimos3Meses
        } else {
            s.sueldoBase
        }

        // ── 1. Remuneración proporcional del último mes ──────────────────────
        // Se calcula automáticamente: día 1 del mes de término hasta fechaTermino inclusive.
        // Ejemplo: si termina el 18 de mayo → 18 días trabajados en ese mes aún no pagados.
        val diasTrabajadosUltimoMes: Int = s.fechaTermino.dayOfMonth
        val remuneracionUltimoMes: Long = ((baseRemuneracion / 30.0) * diasTrabajadosUltimoMes).roundToLong()

        // ── 2. Antigüedad (meses y años completos) ───────────────────────────
        val mesesTotales = ChronoUnit.MONTHS.between(s.fechaInicio, s.fechaTermino).toInt()
        val mesesFraccion = mesesTotales % 12
        val aniosCompletos = mesesTotales / 12
        // Regla Art. 163: fracción > 6 meses se redondea a año adicional
        val aniosParaIAS: Int = when {
            aniosCompletos == 0 -> 0                           // Menos de 1 año: no aplica IAS
            mesesFraccion > 6 -> aniosCompletos + 1
            else -> aniosCompletos
        }

        // ── 3. Indemnización por años de servicio (IAS) ──────────────────────
        // Requiere: causal que genere IAS + al menos 1 año de servicio
        val tieneDerecho = s.causal.generaIAS && aniosCompletos >= 1
        val aniosReconocidos = if (tieneDerecho) min(aniosParaIAS, ConstantesLaborales.TOPE_ANIOS_IAS) else 0
        // Art. 172: tope de 90 UF en la base de cálculo
        val topadaPor90UF = baseRemuneracion > ConstantesLaborales.TOPE_UF_90_PESOS
        val baseCalculoIAS = if (topadaPor90UF) ConstantesLaborales.TOPE_UF_90_PESOS else baseRemuneracion
        val indemnizacionAnios: Long = if (tieneDerecho) baseCalculoIAS * aniosReconocidos else 0L

        // ── 4. Indemnización sustitutiva aviso previo ────────────────────────
        // Solo si la causal lo permite Y el empleador NO dio aviso con 30 días
        val baseCalculoAviso = if (topadaPor90UF) ConstantesLaborales.TOPE_UF_90_PESOS else baseRemuneracion
        val indemnizacionAvisoPrevio: Long = if (s.causal.generaAvisoPrevio && !s.avisoDado) {
            baseCalculoAviso
        } else 0L

        // ── 5. Feriado proporcional ──────────────────────────────────────────
        // 1.25 días por mes trabajado desde la última vez que se tomaron vacaciones completas.
        // Días brutos acumulados = mesesTotales × 1.25
        val diasFeriadoBruto: Double = mesesTotales * ConstantesLaborales.DIAS_VACACIONES_POR_MES
        // Si el usuario indicó que NO tiene vacaciones pendientes, el neto es 0.
        // Si indicó que SÍ, se resta lo que ya tomó.
        val diasFeriadoNeto: Double = if (!s.tieneVacacionesPendientes) {
            0.0
        } else {
            (diasFeriadoBruto - s.diasVacacionesTomadas).coerceAtLeast(0.0)
        }
        // Pago = (sueldo / 30) × días netos
        val feriadoProporcional: Long = ((baseRemuneracion / 30.0) * diasFeriadoNeto).roundToLong()

        // ── 6. Gratificación proporcional (Art. 50) ──────────────────────────
        // Si la empresa ya pagó gratificación mensual, no se adeuda en el finiquito.
        // En caso contrario, calculamos los meses trabajados en el año calendario en curso.
        val gratificacionProporcional: Long
        val mesesGratificacion: Int

        if (s.gratificacionYaPagadaMensual) {
            gratificacionProporcional = 0L
            mesesGratificacion = 0
        } else {
            // Meses trabajados dentro del año calendario del término
            val inicioDelAnio = LocalDate.of(s.fechaTermino.year, 1, 1)
            val inicioEfectivo = if (s.fechaInicio.isAfter(inicioDelAnio)) s.fechaInicio else inicioDelAnio
            mesesGratificacion = ChronoUnit.MONTHS.between(inicioEfectivo, s.fechaTermino).toInt()
                .coerceAtLeast(0)

            // Por mes: menor entre 25% del sueldo y el tope mensual (Art. 50)
            val gratifMensual = min(
                (baseRemuneracion * 0.25).roundToLong(),
                ConstantesLaborales.TOPE_MENSUAL_GRATIFICACION
            )
            gratificacionProporcional = gratifMensual * mesesGratificacion
        }

        // ── 7. Total ─────────────────────────────────────────────────────────
        val costoTotal = remuneracionUltimoMes +
                indemnizacionAnios +
                indemnizacionAvisoPrevio +
                feriadoProporcional +
                gratificacionProporcional

        _result.value = FiniquitoResult(
            remuneracionUltimoMes = remuneracionUltimoMes,
            diasUltimoMes = diasTrabajadosUltimoMes,
            indemnizacionAnios = indemnizacionAnios,
            aniosReconocidos = aniosReconocidos,
            baseCalculoIAS = baseCalculoIAS,
            topadaPor90UF = topadaPor90UF,
            indemnizacionAvisoPrevio = indemnizacionAvisoPrevio,
            feriadoProporcional = feriadoProporcional,
            diasFeriadoBruto = diasFeriadoBruto,
            diasFeriadoNeto = diasFeriadoNeto,
            gratificacionProporcional = gratificacionProporcional,
            mesesGratificacion = mesesGratificacion,
            costoTotal = costoTotal
        )
    }
}
