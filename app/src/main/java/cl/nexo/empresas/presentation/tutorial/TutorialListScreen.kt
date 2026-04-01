package cl.nexo.empresas.presentation.tutorial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.nexo.empresas.core.tutorial.TutorialManager
import cl.nexo.empresas.core.tutorial.TutorialModule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TutorialListViewModel @Inject constructor(
    private val tutorialManager: TutorialManager
) : ViewModel() {

    private val _statuses = MutableStateFlow<Map<TutorialModule, Boolean>>(emptyMap())
    val statuses = _statuses.asStateFlow()

    init {
        loadStatuses()
    }

    fun loadStatuses() {
        viewModelScope.launch {
            _statuses.value = tutorialManager.getAllStatus()
        }
    }

    fun markCompleted(module: TutorialModule) {
        viewModelScope.launch {
            tutorialManager.markTutorialCompleted(module)
            loadStatuses()
        }
    }

    fun resetModule(module: TutorialModule) {
        viewModelScope.launch {
            tutorialManager.resetTutorial(module)
            loadStatuses()
        }
    }

    fun resetAll() {
        viewModelScope.launch {
            tutorialManager.resetAllTutorials()
            loadStatuses()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialListScreen(
    onBack: () -> Unit,
    viewModel: TutorialListViewModel = hiltViewModel()
) {
    val statuses by viewModel.statuses.collectAsState()
    var showTutorial by remember { mutableStateOf<TutorialModule?>(null) }
    var showResetAllDialog by remember { mutableStateOf(false) }
    var showResetModuleDialog by remember { mutableStateOf<TutorialModule?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tutoriales y Ayuda") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { showResetAllDialog = true }) {
                        Icon(Icons.Default.RestartAlt, "Reiniciar todos")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TutorialModule.entries.toList()) { module ->
                val isCompleted = statuses[module] ?: false
                ElevatedCard(
                    onClick = { showTutorial = module },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = module.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = module.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    if (isCompleted) "✓ Completado" else "Pendiente",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isCompleted)
                                    Color(0xFFE8F5E9)
                                else
                                    Color(0xFFFFF3E0)
                            )
                        )
                        // Individual reset button
                        if (isCompleted) {
                            IconButton(
                                onClick = { showResetModuleDialog = module },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Replay,
                                    contentDescription = "Reiniciar ${module.displayName}",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Tutorial overlay — marks as completed when finished
    showTutorial?.let { module ->
        TutorialOverlay(
            module = module,
            onFinish = {
                viewModel.markCompleted(module)
                showTutorial = null
            }
        )
    }

    // Reset individual module dialog
    showResetModuleDialog?.let { module ->
        AlertDialog(
            onDismissRequest = { showResetModuleDialog = null },
            title = { Text("Reiniciar tutorial") },
            text = { Text("¿Reiniciar el tutorial de \"${module.displayName}\"? Volverá a mostrarse la próxima vez que entres a esa pantalla.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetModule(module)
                    showResetModuleDialog = null
                }) {
                    Text("Reiniciar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetModuleDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Reset all dialog
    if (showResetAllDialog) {
        AlertDialog(
            onDismissRequest = { showResetAllDialog = false },
            title = { Text("Reiniciar tutoriales") },
            text = { Text("¿Quieres reiniciar todos los tutoriales? La próxima vez que abras cada módulo verás el tutorial de nuevo.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetAll()
                    showResetAllDialog = false
                }) {
                    Text("Reiniciar todos")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetAllDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
