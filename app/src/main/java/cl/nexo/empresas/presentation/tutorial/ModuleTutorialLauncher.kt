package cl.nexo.empresas.presentation.tutorial

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.nexo.empresas.core.tutorial.TutorialManager
import cl.nexo.empresas.core.tutorial.TutorialModule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel shared for tutorial launching.
 * Each screen passes its TutorialModule; the VM checks if it was completed.
 */
@HiltViewModel
class ModuleTutorialViewModel @Inject constructor(
    private val tutorialManager: TutorialManager
) : ViewModel() {

    private val _showTutorial = MutableStateFlow<TutorialModule?>(null)
    val showTutorial = _showTutorial.asStateFlow()

    fun checkAndLaunch(module: TutorialModule) {
        viewModelScope.launch {
            if (!tutorialManager.isTutorialCompleted(module)) {
                _showTutorial.value = module
            }
        }
    }

    fun dismiss() {
        _showTutorial.value = null
    }
}

/**
 * Drop this composable into any screen's composable to auto-launch
 * the tutorial the first time the user visits.
 *
 * Usage:
 *   ModuleTutorialLauncher(TutorialModule.CHEQUES)
 */
@Composable
fun ModuleTutorialLauncher(
    module: TutorialModule,
    viewModel: ModuleTutorialViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val current by viewModel.showTutorial.collectAsState()

    LaunchedEffect(module) {
        viewModel.checkAndLaunch(module)
    }

    if (current == module) {
        TutorialOverlay(
            module = module,
            onFinish = { viewModel.dismiss() }
        )
    }
}
