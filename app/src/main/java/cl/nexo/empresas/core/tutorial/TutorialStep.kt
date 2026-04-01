package cl.nexo.empresas.core.tutorial

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class TutorialStep(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color = Color(0xFF1565C0),
    val iconBgColor: Color = Color(0xFFE3F2FD)
)
