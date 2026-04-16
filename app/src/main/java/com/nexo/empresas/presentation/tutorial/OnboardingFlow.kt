package com.nexo.empresas.presentation.tutorial

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexo.empresas.core.tutorial.TutorialManager
import com.nexo.empresas.core.tutorial.TutorialModule
import com.nexo.empresas.core.tutorial.TutorialSteps
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val tutorialManager: TutorialManager
) : ViewModel() {

    private val _currentModule = MutableStateFlow<TutorialModule?>(null)
    val currentModule = _currentModule.asStateFlow()

    private val _isComplete = MutableStateFlow(false)
    val isComplete = _isComplete.asStateFlow()

    private val onboardingSequence = listOf(
        TutorialModule.ONBOARDING,
        TutorialModule.HUB
    )

    init {
        viewModelScope.launch {
            advanceToNext()
        }
    }

    private suspend fun advanceToNext() {
        for (module in onboardingSequence) {
            if (!tutorialManager.isTutorialCompleted(module)) {
                _currentModule.value = module
                return
            }
        }
        _isComplete.value = true
    }

    fun onModuleCompleted(module: TutorialModule) {
        viewModelScope.launch {
            tutorialManager.markTutorialCompleted(module)
            _currentModule.value = null
            advanceToNext()
        }
    }
}

@Composable
fun OnboardingFlow(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val currentModule by viewModel.currentModule.collectAsState()
    val isComplete by viewModel.isComplete.collectAsState()

    LaunchedEffect(isComplete) {
        if (isComplete) onComplete()
    }

    currentModule?.let { module ->
        TutorialOverlay(
            steps = TutorialSteps.getSteps(module),
            onDismiss = { viewModel.onModuleCompleted(module) }
        )
    }
}
