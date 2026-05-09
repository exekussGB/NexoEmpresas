package com.nexo.empresas.presentation.simulador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexo.empresas.data.model.*
import com.nexo.empresas.data.model.RemuneracionesChile as RC
import com.nexo.empresas.data.network.IndicadoresService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

@HiltViewModel
class SimuladorViewModel @Inject constructor(
    private val indicadoresService: IndicadoresService
) : ViewModel() {

    private val _input = MutableStateFlow(SimulacionInput())
    val input: StateFlow<SimulacionInput> = _input.asStateFlow()

    private val _result = MutableStateFlow<SimulacionResult?>(null)
    val result: StateFlow<SimulacionResult?> = _result.asStateFlow()

    private val _isFetchingUtm = MutableStateFlow(false)
    val isFetchingUtm: StateFlow<Boolean> = _isFetchingUtm.asStateFlow()

    private val _comparacion = MutableStateFlow<ComparacionEscenarios?>(null)
    val comparacion: StateFlow<ComparacionEscenarios?> = _comparacion.asStateFlow()

    init {
        fetchUtm()
        // Forzar modo DESDE_LIQUIDO y sueldoBase = INGRESO_MINIMO
        _input.value = _input.value.copy(
            modoCalculo = ModoCalculo.DESDE_LIQUIDO,
            sueldoBase = RC.INGRESO_MINIMO
        )
    }

    private fun fetchUtm() {
        viewModelScope.launch {
            _isFetchingUtm.value = true
            val utm = indicadoresService.fetchUtm()
            if (utm != null) {
                RC.UTM = utm
                RC.isUtmUpdated = true
                recalculate()
            }
            _isFetchingUtm.value = false
        }
    }

    fun updateInput(transform: SimulacionInput.() -> SimulacionInput) {
        val oldInput = _input.value
        var newInput = oldInput.transform()

        // Detectar cambios manuales y actualizar flags
        if (newInput.colacion != oldInput.colacion) {
            newInput = newInput.copy(colacionManual = true)
        }
        if (newInput.movilizacion != oldInput.movilizacion) {
            newInput = newInput.copy(movilizacionManual = true)
        }
        if (newInput.bonosNoImponibles != oldInput.bonosNoImponibles) {
            newInput = newInput.copy(otrosNoImponiblesManual = true)
        }

        // Forzar sueldoBase = INGRESO_MINIMO
        _input.value = newInput.copy(sueldoBase = RC.INGRESO_MINIMO)
        recalculate()
    }

    fun reset() {
        _input.value = SimulacionInput(
            modoCalculo = ModoCalculo.DESDE_LIQUIDO,
            sueldoBase = RC.INGRESO_MINIMO
        )
        _result.value = null
        _comparacion.value = null
    }

    private fun recalculate() {
        val i = _input.value

        if (i.sueldoLiquidoDeseado <= 0) {
            _result.value = null
            _comparacion.value = null
            return
        }

        // STEP 1 — Fixed components
        val sueldoBase = RC.INGRESO_MINIMO

        // STEP 2 — Net from fixed base only
        // Simulamos con 0 no imponibles para ver el líquido base legal
        val inputBaseLegal = i.copy(
            colacion = 0, movilizacion = 0, viaticos = 0,
            desgasteHerramientas = 0, bonosNoImponibles = 0,
            horasExtraCount = 0, comisiones = 0, bonosImponibles = 0
        )
        val resBaseLegal = calculateResult(sueldoBase, inputBaseLegal)
        val liquidoDeBase = resBaseLegal.sueldoLiquido

        // STEP 3 — Shortfall
        val shortfall = i.sueldoLiquidoDeseado - liquidoDeBase

        var warningMinimo = false
        var optimizedInput = i

        if (shortfall < 0) {
            warningMinimo = true
        } else if (shortfall > 0) {
            // Auto-suggest non-taxable allowances if not manual
            val sugeridaColacion = min(shortfall, 50_000L)
            val remaining1 = shortfall - sugeridaColacion
            val sugeridaMovilizacion = min(remaining1, 50_000L)
            val remaining2 = remaining1 - sugeridaMovilizacion
            val sugeridaOtros = remaining2

            optimizedInput = i.copy(
                colacion = if (i.colacionManual) i.colacion else sugeridaColacion,
                movilizacion = if (i.movilizacionManual) i.movilizacion else sugeridaMovilizacion,
                bonosNoImponibles = if (i.otrosNoImponiblesManual) i.bonosNoImponibles else sugeridaOtros
            )
        }

        // Actualizar el input con flags de warning
        _input.value = optimizedInput.copy(warningPorDebajoMinimo = warningMinimo)

        val finalResult = calculateResult(sueldoBase, optimizedInput)
        _result.value = finalResult.copy(sueldoLiquidoDeseado = i.sueldoLiquidoDeseado)

        // STEP 5 — Scenario comparison
        calculateComparison(i.sueldoLiquidoDeseado, optimizedInput, finalResult)
    }

    private fun calculateComparison(liquidoDeseado: Long, currentInput: SimulacionInput, currentResult: SimulacionResult) {
        // Escenario "Todo Imponible": 
        // Buscamos un sueldoBase que de el líquidoDeseado con 0 no imponibles
        var low = 1L
        var high = 10_000_000L
        var resTodoImponible: SimulacionResult? = null

        val inputSoloImponible = currentInput.copy(
            colacion = 0, movilizacion = 0, viaticos = 0,
            desgasteHerramientas = 0, bonosNoImponibles = 0
        )

        for (iter in 1..50) {
            val mid = (low + high) / 2
            val res = calculateResult(mid, inputSoloImponible)
            val diff = res.sueldoLiquido - liquidoDeseado

            if (kotlin.math.abs(diff) <= 100) {
                resTodoImponible = res
                break
            }
            if (diff < 0) low = mid + 1 else high = mid - 1
            resTodoImponible = res
        }

        resTodoImponible?.let {
            _comparacion.value = ComparacionEscenarios(
                costoTodoImponible = it.costoTotalEmpresa,
                costoSugerido = currentResult.costoTotalEmpresa
            )
        }
    }

    private fun calculateResult(base: Long, i: SimulacionInput): SimulacionResult {
        // ── 1. Gratificación ──
        val gratificacion = when (i.gratificacionTipo) {
            GratificacionTipo.ART_50 -> {
                // 25% del sueldo base, con tope mensual
                min((base * 0.25).toLong(), RC.TOPE_GRATIFICACION_MENSUAL)
            }
            GratificacionTipo.ART_47 -> (base * 0.30).toLong()
            GratificacionTipo.SIN_GRATIFICACION -> 0L
        }

        // ── 1.1 Horas Extras ──
        val horasExtrasMonto = (base / 30.0 / 8.0 * 1.5 * i.horasExtraCount).roundToLong()

        // ── 2. Totales ──
        val totalImponible = base + gratificacion + i.comisiones +
                i.bonosImponibles + horasExtrasMonto
        val imponibleTopado = min(totalImponible, RC.TOPE_IMPONIBLE)
        val excedeTopeImponible = totalImponible > RC.TOPE_IMPONIBLE

        val totalNoImponible = i.colacion + i.movilizacion + i.viaticos +
                i.desgasteHerramientas + i.bonosNoImponibles
        val totalHaberes = totalImponible + totalNoImponible

        // ── 3. Descuentos trabajador ──
        val afp = RC.AFP_LIST[i.afpIndex]
        val afpMonto = (imponibleTopado * afp.total / 100.0).roundToLong()

        val saludMonto: Long
        val saludDetalle: String
        when (i.tipoSalud) {
            TipoSalud.FONASA -> {
                saludMonto = (imponibleTopado * RC.FONASA_TASA).roundToLong()
                saludDetalle = "Fonasa 7%"
            }
            TipoSalud.ISAPRE -> {
                val minLegal = (imponibleTopado * RC.FONASA_TASA).roundToLong()
                saludMonto = max(minLegal, i.cotizacionIsapre)
                saludDetalle = "Isapre \$${formatCLP(saludMonto)}"
            }
        }

        val cesantiaTrabajador = when (i.tipoContrato) {
            TipoContrato.INDEFINIDO -> (imponibleTopado * RC.CESANTIA_TRABAJADOR_INDEFINIDO).roundToLong()
            else -> 0L
        }

        // ── 4. Impuesto Único (IUSC) ──
        val baseImpuesto = imponibleTopado - afpMonto - saludMonto - cesantiaTrabajador
        val baseEnUtm = baseImpuesto.toDouble() / RC.UTM
        var impuestoUnico = 0L
        for (tramo in RC.TRAMOS_IUSC) {
            if (baseEnUtm > tramo.desde) {
                impuestoUnico = ((baseEnUtm * tramo.tasa - tramo.rebaja) * RC.UTM).roundToLong()
            }
        }
        impuestoUnico = max(0L, impuestoUnico)
        val tasaEfectiva = if (baseImpuesto > 0) {
            (impuestoUnico.toDouble() / baseImpuesto * 100.0)
        } else 0.0

        val totalDescuentos = afpMonto + saludMonto + cesantiaTrabajador + impuestoUnico +
                i.anticipo + i.prestamoEmpresa + i.otrosDescuentos

        // ── 5. Líquido ──
        val sueldoLiquido = totalHaberes - totalDescuentos

        // ── 6. Costos empleador ──
        val sisMonto = (imponibleTopado * RC.SIS_TASA).roundToLong()

        val cesantiaEmpleador = when (i.tipoContrato) {
            TipoContrato.INDEFINIDO -> (imponibleTopado * RC.CESANTIA_EMPLEADOR_INDEFINIDO).roundToLong()
            else -> (imponibleTopado * RC.CESANTIA_EMPLEADOR_PLAZO_FIJO).roundToLong()
        }

        val tasaMutualTotal = RC.MUTUAL_BASE + i.tasaMutualAdicional / 100.0
        val mutualMonto = (imponibleTopado * tasaMutualTotal).roundToLong()

        val totalCostosEmpleador = sisMonto + cesantiaEmpleador + mutualMonto
        val costoTotal = totalHaberes + totalCostosEmpleador

        return SimulacionResult(
            sueldoBase = base,
            gratificacion = gratificacion,
            comisiones = i.comisiones,
            bonosImponibles = i.bonosImponibles,
            horasExtras = horasExtrasMonto,
            totalImponible = totalImponible,
            imponibleTopado = imponibleTopado,
            excedeTopeImponible = excedeTopeImponible,
            colacion = i.colacion,
            movilizacion = i.movilizacion,
            viaticos = i.viaticos,
            desgasteHerramientas = i.desgasteHerramientas,
            bonosNoImponibles = i.bonosNoImponibles,
            totalNoImponible = totalNoImponible,
            totalHaberes = totalHaberes,
            afpNombre = afp.nombre,
            afpMonto = afpMonto,
            saludMonto = saludMonto,
            saludDetalle = saludDetalle,
            cesantiaTrabajador = cesantiaTrabajador,
            baseImponibleImpuesto = baseImpuesto,
            impuestoUnico = impuestoUnico,
            tasaEfectivaImpuesto = tasaEfectiva,
            anticipo = i.anticipo,
            prestamoEmpresa = i.prestamoEmpresa,
            otrosDescuentos = i.otrosDescuentos,
            otrosDescuentosLabel = i.otrosDescuentosLabel,
            totalDescuentosTrabajador = totalDescuentos,
            sueldoLiquido = sueldoLiquido,
            sisMonto = sisMonto,
            cesantiaEmpleador = cesantiaEmpleador,
            mutualMonto = mutualMonto,
            totalCostosEmpleador = totalCostosEmpleador,
            costoTotalEmpresa = costoTotal,
        )
    }

    companion object {
        fun formatCLP(amount: Long): String {
            val formatted = amount.toString().reversed().chunked(3).joinToString(".").reversed()
            return formatted
        }
    }
}
