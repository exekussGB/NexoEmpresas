package cl.nexo.empresas.presentation.navigation

sealed class Screen(val route: String) {
    data object Login         : Screen("login")
    data object Register      : Screen("register")
    data object Empresas      : Screen("empresas")
    data object Hub           : Screen("hub")
    data object Dashboard     : Screen("dashboard")
    data object CuentasCobrar : Screen("cuentas_cobrar")
    data object CuentasPagar  : Screen("cuentas_pagar")
    data object Cheques       : Screen("cheques")
    data object AddDocumento  : Screen("add_documento")
    data object Graficos      : Screen("graficos")
    data object Cuentas       : Screen("cuentas_corrientes")
    data object Contactos     : Screen("contactos")
    data object Opciones      : Screen("opciones")
    data object Alertas       : Screen("alertas")
    data object Scanner : Screen("scanner")
    data object DocumentoDetail : Screen("documento/{documentoId}") {
        fun route(id: String) = "documento/$id"
    }
}
