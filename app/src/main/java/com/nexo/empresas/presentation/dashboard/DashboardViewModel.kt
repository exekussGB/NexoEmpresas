package com.nexo.empresas.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexo.empresas.data.model.DashboardTotales
import com.nexo.empresas.domain.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _totales = MutableStateFlow<DashboardTotales?>(null)
    val totales: StateFlow<DashboardTotales?> = _totales.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadTotales()
    }

    fun loadTotales() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            dashboardRepository.getTotales()
                .onSuccess { _totales.value = it }
                .onFailure { _error.value = it.message ?: "Error al cargar dashboard" }
            _isLoading.value = false
        }
    }
}
