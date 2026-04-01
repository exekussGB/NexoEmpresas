package cl.nexo.empresas.core.tutorial

enum class TutorialModule(
    val key: String,
    val displayName: String,
    val description: String
) {
    ONBOARDING("tutorial_onboarding", "Bienvenida", "Primeros pasos en NexoEmpresas"),
    EMPRESA_SETUP("tutorial_empresa_setup", "Crear Empresa", "Configuración inicial de empresa"),
    EMPRESA_MIEMBROS("tutorial_empresa_miembros", "Miembros y Roles", "Invitar y gestionar el equipo"),
    HUB("tutorial_hub", "Panel Principal", "Navegación por la app"),
    DASHBOARD("tutorial_dashboard", "Dashboard", "Indicadores financieros de tu empresa"),
    DOCUMENTOS("tutorial_documentos", "Documentos", "Facturas por cobrar y pagar"),
    CHEQUES("tutorial_cheques", "Cheques", "Gestión de cheques recibidos y emitidos"),
    CONTACTOS("tutorial_contactos", "Contactos", "Clientes y proveedores"),
    CUENTAS("tutorial_cuentas", "Cuentas Corrientes", "Control de cuentas bancarias"),
    GRAFICOS("tutorial_graficos", "Gráficos", "Análisis visual del negocio"),
    ALERTAS("tutorial_alertas", "Alertas", "Vencimientos y notificaciones"),
    SCANNER("tutorial_scanner", "Escáner DTE", "Lectura de documentos PDF417"),
    SIMULADOR("tutorial_simulador", "Simulador", "Proyecciones financieras")
}
