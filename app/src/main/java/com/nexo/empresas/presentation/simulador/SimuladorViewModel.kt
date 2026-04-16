package com.nexo.empresas.presentation.simulador

import androidx.lifecycle.ViewModel
import com.nexo.empresas.data.model.*
import com.nexo.empresas.data.model.RemuneracionesChile as RC
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

class SimuladorViewModel : ViewModel() {

    private val _input = MutableStateFlow(SimulacionInput())
    val input: StateFlow<SimulacionInput> = _input.asStateFlow()

    private val _result = MutableStateFlow<SimulacionResult?>(null)
    val result: StateFlow<SimulacionResult?> = _result.asStateFlow()

    fun updateInput(transform: SimulacionInput.() -> SimulacionInput) {
        _input.value = _input.value.transform()
        recalculate()
    }

    fun reset() {
        _input.value = SimulacionInput()
        _result.value = null
    }

    private fun recalculate() {
        val i = _input.value
        if (i.sueldoBase <= 0) {
            _result.value = null
            return
        }

        // ── 1. Gratificación ──
        val gratificacion = when (i.gratificacionTipo) {
            GratificacionTipo.ART_50 -> {
                val topeGrat = (RC.TOPE_GRATIFICACION_ART50 * RC.INGRESO_MINIMO).toLong()
                min((i.sueldoBase * 0.25).toLong(), topeGrat)
            }
            GratificacionTipo.ART_47 -> (i.sueldoBase * 0.30).toLong()
            GratificacionTipo.SIN_GRATIFICACION -> 0L
        }

        // ── 2. Totales ──
        val totalImponible = i.sueldoBase + gratificacion + i.comisiones +
                i.bonosImponibles + i.horasExtras
        val imponibleTopado = min(totalImponible, RC.TOPE_IMPONIBLE)
        val excedeTopeImponible = totalImponible > RC.TOPE_IMPONIBLE

        val totalNoImponible = i.colacion + i.movilizacion + i.viaticos +
                i.bonosNoImponibles
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
                // Isapre: el mayor entre 7% legal y la cotización pactada
                val minLegal = (imponibleTopado * RC.FONASA_TASA).roundToLong()
                saludMonto = max(minLegal, i.cotizacionIsapre)
                saludDetalle = "Isapre \$${formatCLP(saludMonto)}"
            }
        }

        val cesantiaTrabajador = when (i.tipoContrato) {
            TipoContrato.INDEFINIDO -> (imponibleTopado * RC.CESANTIA_TRABAJADOR_INDEFINIDO).roundToLong()
            else -> 0L // Plazo fijo y obra: 0% trabajador, todo empleador
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

        val totalDescuentos = afpMonto + saludMonto + cesantiaTrabajador + impuestoUnico

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

        _result.value = SimulacionResult(
            sueldoBase = i.sueldoBase,
            gratificacion = gratificacion,
            comisiones = i.comisiones,
            bonosImponibles = i.bonosImponibles,
            horasExtras = i.horasExtras,
            totalImponible = totalImponible,
            imponibleTopado = imponibleTopado,
            excedeTopeImponible = excedeTopeImponible,
            colacion = i.colacion,
            movilizacion = i.movilizacion,
            viaticos = i.viaticos,
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