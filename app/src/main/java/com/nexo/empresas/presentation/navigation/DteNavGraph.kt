package com.nexo.empresas.presentation.navigation.dte

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.nexo.empresas.presentation.dte.*
import com.nexo.empresas.presentation.dte.scanner.ScannerVerificacionScreen

// ─── Rutas ────────────────────────────────────────────────────────────────────

object DteRoutes {
    const val ROOT = "dte"

    // Lista — recibe empresaId solo para entrar al grafo desde el Hub
    const val LISTA = "dte/lista/{empresaId}"
    fun lista(empresaId: String) = "dte/lista/$empresaId"

    // Las demás pantallas obtienen el empresaId del TenantManager
    const val EMITIR    = "dte/emitir"
    const val FOLIOS    = "dte/folios"
    const val CERT      = "dte/certificado"

    // Detalle sí necesita el dteId
    const val DETALLE   = "dte/detalle/{dteId}"
    fun detalle(dteId: String) = "dte/detalle/$dteId"

    const val SCANNER   = "dte/scanner"
}

// ─── Grafo de navegación ──────────────────────────────────────────────────────

fun NavGraphBuilder.dteNavGraph(navController: NavHostController) {
    navigation(
        startDestination = DteRoutes.LISTA,
        route = DteRoutes.ROOT
    ) {
        // Lista de DTEs
        composable(
            route = DteRoutes.LISTA,
            arguments = listOf(navArgument("empresaId") { type = NavType.StringType })
        ) {
            ListaDtesScreen(
                onNavigateToDetalle = { dteId ->
                    navController.navigate(DteRoutes.detalle(dteId))
                },
                onNavigateToEmitir = {
                    navController.navigate(DteRoutes.EMITIR)
                },
                onNavigateToScanner = {
                    navController.navigate(DteRoutes.SCANNER)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Emitir DTE
        composable(route = DteRoutes.EMITIR) {
            EmitirDteScreen(
                onNavigateBack = { navController.popBackStack() },
                onDteEmitido = { dteId ->
                    navController.navigate(DteRoutes.detalle(dteId)) {
                        popUpTo(DteRoutes.EMITIR) { inclusive = true }
                    }
                }
            )
        }

        // Detalle DTE
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

        // Folios
        composable(route = DteRoutes.FOLIOS) {
            FoliosScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Scanner Verificación
        composable(route = DteRoutes.SCANNER) {
            ScannerVerificacionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Certificado / Onboarding
        composable(route = DteRoutes.CERT) {
            OnboardingCertificadoScreen(
                onNavigateBack = { navController.popBackStack() },
                onCertificadoRegistrado = {
                    navController.popBackStack()
                }
            )
        }
    }
}
