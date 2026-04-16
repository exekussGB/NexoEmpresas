package com.nexo.empresas.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexo.empresas.core.session.SessionManager
import com.nexo.empresas.data.model.Empresa
import com.nexo.empresas.domain.repository.EmpresasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val empresasRepository: EmpresasRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _empresa = MutableStateFlow<Empresa?>(null)
    val empresa: StateFlow<Empresa?> = _empresa.asStateFlow()

    val userEmail: String get() = sessionManager.userEmail
    val userRole: String get() = sessionManager.userRole

    val inviteCode: StateFlow<String> get() = _inviteCode
    private val _inviteCode = MutableStateFlow("")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadEmpresa()
    }

    private fun loadEmpresa() {
        viewModelScope.launch {
            _isLoading.value = true
            empresasRepository.getEmpresasForUser()
                .onSuccess { empresas ->
                    val emp = empresas.find { it.id == sessionManager.empresaId }
                    _empresa.value = emp
                    _inviteCode.value = emp?.inviteCode ?: ""
                }
                .onFailure { /* silently ignore */ }
            _isLoading.value = false
        }
    }

    fun logout() {
        sessionManager.clearSession()
    }
}
