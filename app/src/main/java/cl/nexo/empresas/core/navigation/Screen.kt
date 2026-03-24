package cl.nexo.empresas.core.navigation

sealed class Screen(val route: String) {
    // Auth
    data object Login        : Screen("login")
    data object Register     : Screen("register")
    // Empresa selection
    data object Empresas     : Screen("empresas")
    // Inside empresa
    data object Hub          : Screen("hub")
    data object Dashboard    : Screen("dashboard")
    data object CuentasCobrar: Screen("cuentas_cobrar")
    data object CuentasPagar : Screen("cuentas_pagar")
    data object Cheques      : Screen("cheques")
    data object AddDocumento : Screen("add_documento")
    data object Graficos     : Screen("graficos")
    data object Cuentas      : Screen("cuentas_corrientes")
    data object Contactos    : Screen("contactos")
    data object Opciones     : Screen("opciones")
    // Detail
    data object DocumentoDetail : Screen("documento/{documentoId}") {
        fun route(id: String) = "documento/$id"
    }
}
