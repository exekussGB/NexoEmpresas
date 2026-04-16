package com.nexoempresas.dte.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.nexoempresas.dte.ui.dte.*

// ─── Rutas ────────────────────────────────────────────────────────────────────

object DteRoutes {
    const val ROOT = "dte"

    // Lista de DTEs
    const val LISTA = "dte/lista/{empresaId}"
    fun lista(empresaId: String) = "dte/lista/$empresaId"

    // Emitir nuevo DTE
    const val EMITIR = "dte/emitir/{empresaId}"
    fun emitir(empresaId: String) = "dte/emitir/$empresaId"

    // Detalle de un DTE
    const val DETALLE = "dte/detalle/{dteId}"
    fun detalle(dteId: String) = "dte/detalle/$dteId"

    // Gestión de folios
    const val FOLIOS = "dte/folios/{empresaId}"
    fun folios(empresaId: String) = "dte/folios/$empresaId"

    // Onboarding certificado
    const val CERTIFICADO = "dte/certificado/{empresaId}"
    fun certificado(empresaId: String) = "dte/certificado/$empresaId"
}

// ─── Grafo de navegación del módulo DTE ──────────────────────────────────────

fun NavGraphBuilder.dteNavGraph(navController: NavHostController) {
    navigation(
        startDestination = DteRoutes.LISTA,
        route = DteRoutes.ROOT
    ) {
        // ── Lista de DTEs ──────────────────────────────────────────────────
        composable(
            route = DteRoutes.LISTA,
            arguments = listOf(navArgument("empresaId") { type = NavType.StringType })
        ) { backStack ->
            val empresaId = backStack.arguments?.getString("empresaId") ?: return@composable
            ListaDtesScreen(
                empresaId = empresaId,
                onNavigateToDetalle = { dteId ->
                    navController.navigate(DteRoutes.detalle(dteId))
                },
                onNavigateToEmitir = {
                    navController.navigate(DteRoutes.emitir(empresaId))
                }
            )
        }

        // ── Emitir DTE ─────────────────────────────────────────────────────
        composable(
            route = DteRoutes.EMITIR,
            arguments = listOf(navArgument("empresaId") { type = NavType.StringType })
        ) { backStack ->
            val empresaId = backStack.arguments?.getString("empresaId") ?: return@composable
            EmitirDteScreen(
                empresaId = empresaId,
                onNavigateBack = { navController.popBackStack() },
                onDteEmitido = { dteId ->
                    navController.navigate(DteRoutes.detalle(dteId)) {
                        popUpTo(DteRoutes.lista(empresaId))
                    }
                }
            )
        }

        // ── Detalle DTE ────────────────────────────────────────────────────
        composable(
            route = DteRoutes.DETALLE,
            arguments = listOf(navArgument("dteId") { type = NavType.StringType })
        ) { backStack ->
            val dteId = backStack.arguments?.getString("dteId") ?: return@composable
            DetalleDteScreen(
                dteId = dteId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Folios ─────────────────────────────────────────────────────────
        composable(
            route = DteRoutes.FOLIOS,
            arguments = listOf(navArgument("empresaId") { type = NavType.StringType })
        ) { backStack ->
            val empresaId = backStack.arguments?.getString("empresaId") ?: return@composable
            FoliosScreen(
                empresaId = empresaId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Certificado / Onboarding ───────────────────────────────────────
        composable(
            route = DteRoutes.CERTIFICADO,
            arguments = listOf(navArgument("empresaId") { type = NavType.StringType })
        ) { backStack ->
            val empresaId = backStack.arguments?.getString("empresaId") ?: return@composable
            OnboardingCertificadoScreen(
                empresaId = empresaId,
                onNavigateBack = { navController.popBackStack() },
                onCertificadoRegistrado = {
                    navController.navigate(DteRoutes.lista(empresaId)) {
                        popUpTo(DteRoutes.certificado(empresaId)) { inclusive = true }
                    }
                }
            )
        }
    }
}
