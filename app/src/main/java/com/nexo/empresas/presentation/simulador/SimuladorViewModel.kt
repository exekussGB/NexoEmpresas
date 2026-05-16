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
        if (newInput.viaticos != oldInput.viaticos) {
            newInput = newInput.copy(viaticosManual = true)
        }
        if (newInput.desgasteHerramientas != oldInput.desgasteHerramientas) {
            newInput = newInput.copy(desgasteHerramientasManual = true)
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

        // 1. Calcular líquido base legal (Sueldo mínimo + mandatory bonuses)
        // Usamos una copia limpia sin ningún haber no imponible
        val inputBaseLegal = i.copy(
            colacion = 0, movilizacion = 0, viaticos = 0,
            desgasteHerramientas = 0, bonosNoImponibles = 0
        )
        val resBaseLegal = calculateResult(RC.INGRESO_MINIMO, inputBaseLegal)
        val liquidoDeBase = resBaseLegal.sueldoLiquido

        // 2. Shortfall: Cuánto falta para llegar al líquido deseado
        val shortfall = i.sueldoLiquidoDeseado - liquidoDeBase

        var warningMinimo = false
        var errorExcedido = false
        val optimizedInput: SimulacionInput

        if (shortfall < 0) {
            warningMinimo = true
            // Si el mínimo ya lo supera, los no imponibles sugeridos deben ser 0
            optimizedInput = i.copy(
                colacion = if (i.colacionManual) i.colacion else 0L,
                movilizacion = if (i.movilizacionManual) i.movilizacion else 0L,
                viaticos = if (i.viaticosManual) i.viaticos else 0L,
                desgasteHerramientas = if (i.desgasteHerramientasManual) i.desgasteHerramientas else 0L,
                bonosNoImponibles = if (i.otrosNoImponiblesManual) i.bonosNoImponibles else 0L
            )
        } else {
            // Sumamos lo que el usuario fijó manualmente
            val fixedManual = (if (i.colacionManual) i.colacion else 0L) +
                    (if (i.movilizacionManual) i.movilizacion else 0L) +
                    (if (i.viaticosManual) i.viaticos else 0L) +
                    (if (i.desgasteHerramientasManual) i.desgasteHerramientas else 0L) +
                    (if (i.otrosNoImponiblesManual) i.bonosNoImponibles else 0L)

            if (fixedManual > shortfall) {
                errorExcedido = true
                // Mantener los manuales, pero los automáticos a 0
                optimizedInput = i.copy(
                    colacion = if (i.colacionManual) i.colacion else 0L,
                    movilizacion = if (i.movilizacionManual) i.movilizacion else 0L,
                    viaticos = if (i.viaticosManual) i.viaticos else 0L,
                    desgasteHerramientas = if (i.desgasteHerramientasManual) i.desgasteHerramientas else 0L,
                    bonosNoImponibles = if (i.otrosNoImponiblesManual) i.bonosNoImponibles else 0L
                )
            } else {
                // Distribuir el restante entre lo no manual
                var remaining = shortfall - fixedManual
                
                // Prioridad 1: Colación (hasta 50k)
                val finalColacion = if (i.colacionManual) i.colacion else {
                    val sug = min(remaining, 50000L)
                    remaining -= sug
                    sug
                }

                // Prioridad 2: Movilización (hasta 50k)
                val finalMovilizacion = if (i.movilizacionManual) i.movilizacion else {
                    val sug = min(remaining, 50000L)
                    remaining -= sug
                    sug
                }

                // Prioridad 3: Otros no imponibles (el resto)
                val finalOtros = if (i.otrosNoImponiblesManual) i.bonosNoImponibles else {
                    val sug = remaining
                    sug
                }
                
                // Viáticos y Desgaste Herr. no se auto-llenan si no son manuales (quedan en 0)
                val finalViaticos = if (i.viaticosManual) i.viaticos else 0L
                val finalDesgaste = if (i.desgasteHerramientasManual) i.desgasteHerramientas else 0L

                optimizedInput = i.copy(
                    colacion = finalColacion,
                    movilizacion = finalMovilizacion,
                    viaticos = finalViaticos,
                    desgasteHerramientas = finalDesgaste,
                    bonosNoImponibles = finalOtros
                )
            }
        }

        // Actualizar el input con flags de estado
        _input.value = optimizedInput.copy(
            warningPorDebajoMinimo = warningMinimo,
            errorSueldoExcedido = errorExcedido
        )

        val finalResult = calculateResult(RC.INGRESO_MINIMO, optimizedInput)
        _result.value = finalResult.copy(sueldoLiquidoDeseado = i.sueldoLiquidoDeseado)

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
