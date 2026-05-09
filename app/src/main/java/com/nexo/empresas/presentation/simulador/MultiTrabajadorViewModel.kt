package com.nexo.empresas.presentation.simulador

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MultiTrabajadorViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(MultiTrabajadorState())
    val state: StateFlow<MultiTrabajadorState> = _state.asStateFlow()

    private val _result = MutableStateFlow<MultiTrabajadorResult?>(null)
    val result: StateFlow<MultiTrabajadorResult?> = _result.asStateFlow()

    fun updateState(transform: MultiTrabajadorState.() -> MultiTrabajadorState) {
        _state.value = _state.value.transform()
        calculate()
    }

    private fun calculate() {
        val s = _state.value
        if (s.costoUnitario <= 0) {
            _result.value = null
            return
        }

        val mensual = s.costoUnitario * s.cantidad
        val anual = mensual * 12
        val anualConGrat = mensual * 13

        val imponibleTotal = s.imponibleUnitario * s.cantidad
        val noImponibleTotal = s.noImponibleUnitario * s.cantidad
        val cotizacionesTotal = s.cotizacionesUnitario * s.cantidad

        _result.value = MultiTrabajadorResult(
            mensual = mensual,
            anual = anual,
            anualConGrat = anualConGrat,
            imponibleTotal = imponibleTotal,
            noImponibleTotal = noImponibleTotal,
            cotizacionesTotal = cotizacionesTotal
        )
    }
}

data class MultiTrabajadorState(
    val costoUnitario: Long = 0,
    val cantidad: Int = 1,
    val imponibleUnitario: Long = 0,
    val noImponibleUnitario: Long = 0,
    val cotizacionesUnitario: Long = 0
)

data class MultiTrabajadorResult(
    val mensual: Long,
    val anual: Long,
    val anualConGrat: Long,
    val imponibleTotal: Long,
    val noImponibleTotal: Long,
    val cotizacionesTotal: Long
)
