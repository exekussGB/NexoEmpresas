package cl.nexo.empresas.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val primaryBlue = Color(0xFF1565C0)
private val incomeGreen = Color(0xFF2E7D32)
private val expenseRed  = Color(0xFFC62828)
private val balanceBlue = Color(0xFF0D47A1)

private val LightColors = lightColorScheme(
    primary = primaryBlue,
    onPrimary = Color.White,
    secondary = incomeGreen,
    tertiary = expenseRed,
    background = Color(0xFFF5F5F5),
    surface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D2B4A),
    secondary = Color(0xFF81C784),
    tertiary = Color(0xFFEF9A9A),
)

@Composable
fun NexoEmpresasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
