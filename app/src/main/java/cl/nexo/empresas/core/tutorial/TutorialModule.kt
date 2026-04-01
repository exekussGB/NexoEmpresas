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
    RESUMEN("tutorial_resumen", "Resumen Financiero", "Estado general de tu empresa"),
    INGRESAR_DOC("tutorial_ingresar_doc", "Ingresar Documento", "Cómo registrar facturas y boletas"),
    POR_PAGAR("tutorial_por_pagar", "Por Pagar", "Documentos que debes pagar"),
    POR_COBRAR("tutorial_por_cobrar", "Por Cobrar", "Documentos que te deben"),
    CHEQUES("tutorial_cheques", "Cheques", "Gestión de cheques recibidos y emitidos"),
    CONTACTOS("tutorial_contactos", "Contactos", "Clientes y proveedores"),
    CUENTAS("tutorial_cuentas", "Cuentas Corrientes", "Control de cuentas bancarias"),
    GRAFICOS("tutorial_graficos", "Gráficos", "Análisis visual del negocio"),
    ALERTAS("tutorial_alertas", "Alertas", "Vencimientos y notificaciones"),
    SCANNER("tutorial_scanner", "Escáner DTE", "Lectura de documentos tributarios"),
    SIMULADOR("tutorial_simulador", "Simulador de Contratación", "Calcula el costo real de contratar")
}
