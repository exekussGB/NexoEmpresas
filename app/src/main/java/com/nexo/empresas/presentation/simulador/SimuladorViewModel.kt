package com.nexo.empresas.presentation.simulador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexo.empresas.data.model.*
import com.nexo.empresas.data.model.RemuneracionesChile as RC
import com.nexo.empresas.data.network.IndicadoresService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

class SimuladorViewModel : ViewModel() {

    private val indicadoresService = IndicadoresService()

    private val _input = MutableStateFlow(SimulacionInput())
    val input: StateFlow<SimulacionInput> = _input.asStateFlow()

    private val _result = MutableStateFlow<SimulacionResult?>(null)
    val result: StateFlow<SimulacionResult?> = _result.asStateFlow()

    private val _isFetchingUtm = MutableStateFlow(false)
    val isFetchingUtm: StateFlow<Boolean> = _isFetchingUtm.asStateFlow()

    // Tracking manual edits to hide "sugerido" badges
    private val _manualFields = MutableStateFlow(setOf<String>())
    val manualFields: StateFlow<Set<String>> = _manualFields.asStateFlow()

    private val _comparacion = MutableStateFlow<ComparacionEscenarios?>(null)
    val comparacion: StateFlow<ComparacionEscenarios?> = _comparacion.asStateFlow()

    init {
        fetchUtm()
        // Asegurar modo DESDE_LIQUIDO por defecto y sueldoBase fijo
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
                RC.UTM = utm.roundToLong()
                RC.isUtmUpdated = true
                recalculate()
            }
            _isFetchingUtm.value = false
        }
    }

    fun updateInput(transform: SimulacionInput.() -> SimulacionInput) {
        val oldInput = _input.value
        val newInput = oldInput.transform()

        // Detectar cambios manuales
        val fields = mutableSetOf<String>()
        if (newInput.colacion != oldInput.colacion) fields.add("colacion")
        if (newInput.movilizacion != oldInput.movilizacion) fields.add("movilizacion")
        if (newInput.bonosNoImponibles != oldInput.bonosNoImponibles) fields.add("otrosNoImponibles")
        
        if (fields.isNotEmpty()) {
            _manualFields.value = _manualFields.value + fields
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
        _manualFields.value = emptySet()
        _result.value = null
        _comparacion.value = null
    }

    private fun recalculate() {
        val i = _input.value

        if (i.modoCalculo == ModoCalculo.DESDE_BRUTO) {
            // Este modo ya no se usa según el requerimiento, pero lo mantenemos por compatibilidad
            if (i.sueldoBase <= 0) {
                _result.value = null
                return
            }
            _result.value = calculateResult(i.sueldoBase, i)
        } else {
            // Modo OPTIMIZADO (Fixed Base + shortfall)
            if (i.sueldoLiquidoDeseado <= 0) {
                _result.value = null
                _comparacion.value = null
                return
            }

            // 1. Calcular líquido base con sueldo mínimo
            val baseResult = calculateResult(RC.INGRESO_MINIMO, i.copy(
                colacion = 0, movilizacion = 0, viaticos = 0,
                desgasteHerramientas = 0, bonosNoImponibles = 0
            ))
            
            val shortfall = i.sueldoLiquidoDeseado - baseResult.sueldoLiquido
            
            if (shortfall <= 0) {
                // El mínimo ya cubre el deseo
                _result.value = baseResult
                _comparacion.value = null
                return
            }

            // 2. Sugerir haberes no imponibles si no han sido editados manualmente
            var suggestedColacion = i.colacion
            var suggestedMovilizacion = i.movilizacion
            var suggestedOtros = i.bonosNoImponibles
            
            if ("colacion" !in _manualFields.value) {
                suggestedColacion = min(shortfall, 50000L)
            }
            val remainingAfterCol = shortfall - suggestedColacion
            
            if ("movilizacion" !in _manualFields.value) {
                suggestedMovilizacion = if (remainingAfterCol > 0) min(remainingAfterCol, 50000L) else 0L
            }
            val remainingAfterMov = remainingAfterCol - suggestedMovilizacion
            
            if ("otrosNoImponibles" !in _manualFields.value) {
                suggestedOtros = max(0L, remainingAfterMov)
            }

            val optimizedInput = i.copy(
                colacion = suggestedColacion,
                movilizacion = suggestedMovilizacion,
                bonosNoImponibles = suggestedOtros
            )
            
            // Actualizar el input visible (sin disparar recalculate infinito)
            _input.value = optimizedInput

            val finalResult = calculateResult(RC.INGRESO_MINIMO, optimizedInput)
            _result.value = finalResult.copy(sueldoLiquidoDeseado = i.sueldoLiquidoDeseado)

            // 3. Calcular escenario de comparación (Todo Imponible)
            calculateComparison(i.sueldoLiquidoDeseado, optimizedInput, finalResult)
        }
    }

    private fun calculateComparison(liquidoDeseado: Long, currentInput: SimulacionInput, currentResult: SimulacionResult) {
        // Escenario "Todo Imponible": 
        // Buscamos un sueldoBase que de el líquidoDeseado con 0 no imponibles
        var low = 1L
        var high = 10_000_000L
        var foundBase = low
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
                foundBase = mid
                resTodoImponible = res
                break
            }
            if (diff < 0) low = mid + 1 else high = mid - 1
            foundBase = mid
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
