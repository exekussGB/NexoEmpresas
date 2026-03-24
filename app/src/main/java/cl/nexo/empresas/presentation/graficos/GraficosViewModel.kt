package cl.nexo.empresas.presentation.graficos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val tenantManager: TenantManager
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
        val empresaId = tenantManager.empresaId ?: return
        viewModelScope.launch {
            _uiState.value = GraficosUiState.Loading
            repo.getGraficoData(empresaId, _meses.value)
                .onSuccess { _uiState.value = GraficosUiState.Success(it) }
                .onFailure { _uiState.value = GraficosUiState.Error(it.message ?: "Error al cargar gráficos") }
        }
    }
}

sealed class GraficosUiState {
    data object Loading : GraficosUiState()
    data class Success(val data: GraficoData) : GraficosUiState()
    data class Error(val message: String) : GraficosUiState()
}
