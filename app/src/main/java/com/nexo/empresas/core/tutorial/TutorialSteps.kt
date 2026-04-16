package com.nexo.empresas.core.tutorial

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.ui.graphics.Color

object TutorialSteps {

    fun getSteps(module: TutorialModule): List<TutorialStep> = when (module) {
        TutorialModule.ONBOARDING -> onboardingSteps()
        TutorialModule.EMPRESA_SETUP -> empresaSetupSteps()
        TutorialModule.EMPRESA_MIEMBROS -> empresaMiembrosSteps()
        TutorialModule.HUB -> hubSteps()
        TutorialModule.DASHBOARD -> dashboardSteps()
        TutorialModule.DOCUMENTOS -> documentosSteps()
        TutorialModule.CHEQUES -> chequesSteps()
        TutorialModule.CONTACTOS -> contactosSteps()
        TutorialModule.CUENTAS -> cuentasSteps()
        TutorialModule.GRAFICOS -> graficosSteps()
        TutorialModule.ALERTAS -> alertasSteps()
        TutorialModule.SCANNER -> scannerSteps()
        TutorialModule.SIMULADOR -> simuladorSteps()
    }

    private fun onboardingSteps() = listOf(
        TutorialStep(
            title = "¡Bienvenido a NexoEmpresas!",
            description = "Tu plataforma de gestión financiera para pymes. Controla facturas, cheques, cuentas y más desde un solo lugar.",
            icon = Icons.Default.Business,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Tu empresa, tu espacio",
            description = "Cada empresa tiene su propio espacio de datos. Puedes administrar múltiples empresas desde una sola cuenta.",
            icon = Icons.Default.Domain,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Trabajo en equipo",
            description = "Invita a tu contador, socio o equipo. Cada miembro tiene un rol con permisos específicos.",
            icon = Icons.Default.Group,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        ),
        TutorialStep(
            title = "Comencemos",
            description = "Primero crea o únete a una empresa. Luego explora el panel principal con todos los módulos disponibles.",
            icon = Icons.Default.PlayArrow,
            iconColor = Color(0xFFF57F17),
            iconBgColor = Color(0xFFFFF8E1)
        )
    )

    private fun empresaSetupSteps() = listOf(
        TutorialStep(
            title = "Datos de tu empresa",
            description = "Ingresa el nombre y RUT de tu empresa. El RUT se valida automáticamente con formato chileno (XX.XXX.XXX-X).",
            icon = Icons.Default.Edit,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Logo opcional",
            description = "Puedes subir el logo de tu empresa. Aparecerá en el header de la app.",
            icon = Icons.Default.Image,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Empresa creada",
            description = "¡Listo! Eres el Dueño de esta empresa. Solo tú puedes eliminarla o transferir la propiedad.",
            icon = Icons.Default.CheckCircle,
            iconColor = Color(0xFF00695C),
            iconBgColor = Color(0xFFE0F7FA)
        ),
        TutorialStep(
            title = "Invita a tu equipo",
            description = "Comparte el código de invitación con tu contador o colaboradores. Elige si entran como Administrador o Visualizador.",
            icon = Icons.Default.PersonAdd,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        ),
        TutorialStep(
            title = "Múltiples empresas",
            description = "Si administras más de una empresa, puedes crearlas todas desde Configuración. Cambia entre ellas con un toque.",
            icon = Icons.Default.SwapHoriz,
            iconColor = Color(0xFFF57F17),
            iconBgColor = Color(0xFFFFF8E1)
        )
    )

    private fun empresaMiembrosSteps() = listOf(
        TutorialStep(
            title = "Miembros del equipo",
            description = "Aquí ves todos los miembros de tu empresa y sus roles: Dueño, Administrador o Visualizador.",
            icon = Icons.Default.Group,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Roles y permisos",
            description = "El Dueño tiene control total. Los Administradores pueden gestionar datos. Los Visualizadores solo pueden consultar información.",
            icon = Icons.Default.Security,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Código de invitación",
            description = "Comparte el código único de tu empresa para que nuevos miembros se unan desde su propia cuenta.",
            icon = Icons.Default.Share,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        )
    )

    private fun hubSteps() = listOf(
        TutorialStep(
            title = "Panel Principal",
            description = "Desde aquí accedes a todos los módulos de tu empresa. El nombre de tu empresa aparece en la parte superior.",
            icon = Icons.Default.Home,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Documentos (CxC / CxP)",
            description = "Registra facturas por cobrar (ingresos) y por pagar (egresos). El botón diferencia automáticamente el tipo.",
            icon = Icons.Default.Description,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Cheques y Cuentas",
            description = "Lleva el control de cheques recibidos/emitidos y los saldos de tus cuentas corrientes.",
            icon = Icons.Default.AccountBalance,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        ),
        TutorialStep(
            title = "Gráficos y Dashboard",
            description = "Visualiza el estado financiero de tu empresa: flujo de caja, documentos pendientes y evolución mensual.",
            icon = Icons.Default.BarChart,
            iconColor = Color(0xFFF57F17),
            iconBgColor = Color(0xFFFFF8E1)
        ),
        TutorialStep(
            title = "Escáner DTE",
            description = "Lee documentos tributarios electrónicos usando el código PDF417 del papel. Extrae el RUT y monto automáticamente.",
            icon = Icons.Default.QrCodeScanner,
            iconColor = Color(0xFFC62828),
            iconBgColor = Color(0xFFFFEBEE)
        )
    )

    private fun dashboardSteps() = listOf(
        TutorialStep(
            title = "Panel financiero",
            description = "Resumen ejecutivo del estado de tu empresa: total por cobrar, total por pagar, saldo de cuentas y flujo del mes.",
            icon = Icons.Default.Dashboard,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Indicadores clave",
            description = "Los KPIs se calculan desde tus documentos activos. Un documento pagado deja de sumarse al pendiente inmediatamente.",
            icon = Icons.Default.TrendingUp,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Función RPC",
            description = "Los totales del dashboard se calculan en el servidor para mayor eficiencia y consistencia de los datos.",
            icon = Icons.Default.Cloud,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        ),
        TutorialStep(
            title = "Empresa activa",
            description = "Los datos que ves corresponden a la empresa seleccionada. Cambia de empresa en Configuración para ver otra.",
            icon = Icons.Default.SwapHoriz,
            iconColor = Color(0xFFF57F17),
            iconBgColor = Color(0xFFFFF8E1)
        )
    )

    private fun documentosSteps() = listOf(
        TutorialStep(
            title = "Documentos: ingreso y egreso",
            description = "Los documentos son el corazón de la app. Registra facturas que te deben (ingreso) y que debes pagar (egreso).",
            icon = Icons.Default.Description,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Agregar documento",
            description = "Al crear un documento indica el tipo (factura, boleta, nota de crédito), número, monto, fecha de vencimiento y contacto asociado.",
            icon = Icons.Default.NoteAdd,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Número de factura",
            description = "El número de factura acepta solo dígitos. Es requerido y debe ser único para evitar duplicados.",
            icon = Icons.Default.Pin,
            iconColor = Color(0xFF00695C),
            iconBgColor = Color(0xFFE0F7FA)
        ),
        TutorialStep(
            title = "Pago relacionado",
            description = "Cuando cobres o pagues una factura, crea un documento de pago vinculado. El sistema marcará el original como pagado automáticamente.",
            icon = Icons.Default.Payment,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        ),
        TutorialStep(
            title = "Filtros y búsqueda",
            description = "Filtra por estado (pendiente / pagado / vencido), rango de fechas o contacto para encontrar cualquier documento rápidamente.",
            icon = Icons.Default.FilterList,
            iconColor = Color(0xFFF57F17),
            iconBgColor = Color(0xFFFFF8E1)
        ),
        TutorialStep(
            title = "Categorías personalizadas",
            description = "Puedes crear categorías propias además de las predeterminadas para organizar tus documentos según el flujo de tu empresa.",
            icon = Icons.Default.Category,
            iconColor = Color(0xFFC62828),
            iconBgColor = Color(0xFFFFEBEE)
        )
    )

    private fun chequesSteps() = listOf(
        TutorialStep(
            title = "Gestión de cheques",
            description = "Registra cheques que recibes de clientes o que emites a proveedores. Asocia cada cheque a un documento para trazabilidad.",
            icon = Icons.Default.Receipt,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Datos del cheque",
            description = "Ingresa número de cheque, banco, fecha de cobro/pago y monto. La fecha es especialmente importante para proyección de flujo de caja.",
            icon = Icons.Default.Edit,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Estados de cheque",
            description = "Un cheque puede estar: Pendiente → En cobro → Cobrado/Depositado o Rechazado. Actualiza el estado conforme avanza el proceso.",
            icon = Icons.Default.Sync,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        ),
        TutorialStep(
            title = "Orden de cobro",
            description = "Los cheques se ordenan por fecha ascendente para que siempre veas primero los que vencen antes.",
            icon = Icons.Default.Sort,
            iconColor = Color(0xFFF57F17),
            iconBgColor = Color(0xFFFFF8E1)
        )
    )

    private fun contactosSteps() = listOf(
        TutorialStep(
            title = "Clientes y proveedores",
            description = "Crea una ficha por cada cliente o proveedor. Asocia documentos y cheques a sus contactos para historial completo.",
            icon = Icons.Default.Contacts,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Validación de RUT",
            description = "El RUT se autoformatea mientras escribes (XX.XXX.XXX-X) y se valida con el algoritmo oficial chileno.",
            icon = Icons.Default.Verified,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Búsqueda rápida",
            description = "Busca por nombre o RUT desde la lista de contactos. El historial de documentos por contacto está disponible en su perfil.",
            icon = Icons.Default.Search,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        ),
        TutorialStep(
            title = "Datos de contacto",
            description = "Agrega teléfono, email y dirección para tener toda la información del contacto en un solo lugar.",
            icon = Icons.Default.ContactPhone,
            iconColor = Color(0xFFF57F17),
            iconBgColor = Color(0xFFFFF8E1)
        )
    )

    private fun cuentasSteps() = listOf(
        TutorialStep(
            title = "Cuentas corrientes",
            description = "Registra las cuentas bancarias de tu empresa. Lleva el saldo actualizado y el detalle de movimientos.",
            icon = Icons.Default.AccountBalance,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Saldo actual",
            description = "El saldo se actualiza automáticamente cuando registras depósitos o egresos asociados a la cuenta.",
            icon = Icons.Default.AttachMoney,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Conciliación",
            description = "Compara el saldo de la app con tu cartola bancaria para detectar diferencias y mantener la contabilidad al día.",
            icon = Icons.Default.CompareArrows,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        )
    )

    private fun graficosSteps() = listOf(
        TutorialStep(
            title = "Análisis visual",
            description = "Gráficos de barras y líneas para visualizar tu flujo de caja, documentos por mes y evolución de ingresos vs egresos.",
            icon = Icons.Default.BarChart,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Filtros de período",
            description = "Selecciona el rango de fechas que quieres analizar. Los gráficos se actualizan en tiempo real.",
            icon = Icons.Default.DateRange,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Exportar datos",
            description = "Próximamente podrás exportar los datos de los gráficos a PDF o Excel para compartir con tu contador.",
            icon = Icons.Default.FileDownload,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        )
    )

    private fun alertasSteps() = listOf(
        TutorialStep(
            title = "Sistema de alertas",
            description = "Recibe notificaciones antes de que venzan tus documentos. Configura con cuántos días de anticipación quieres ser avisado.",
            icon = Icons.Default.Notifications,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Worker en background",
            description = "Las alertas se verifican periódicamente incluso cuando la app está cerrada, gracias al sistema de verificación automática.",
            icon = Icons.Default.Schedule,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Notificaciones push",
            description = "Si tu dispositivo tiene internet, también puedes recibir alertas instantáneas cuando un documento está por vencer.",
            icon = Icons.Default.PhoneAndroid,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        )
    )

    private fun scannerSteps() = listOf(
        TutorialStep(
            title = "Escáner de documentos DTE",
            description = "Lee documentos tributarios electrónicos. Apunta la cámara al código PDF417 que aparece en la parte inferior de la factura impresa.",
            icon = Icons.Default.QrCodeScanner,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Datos extraídos",
            description = "El escáner extrae automáticamente: tipo de documento, folio, RUT emisor, RUT receptor, monto neto, IVA y total.",
            icon = Icons.Default.DataObject,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Crear documento desde scan",
            description = "Una vez escaneado, puedes crear un documento directamente con los datos extraídos. Revisa y confirma antes de guardar.",
            icon = Icons.Default.NoteAdd,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        ),
        TutorialStep(
            title = "Simulador de datos",
            description = "Si no tienes un documento físico, usa el Simulador para ingresar datos manualmente y probar el flujo completo.",
            icon = Icons.Default.Calculate,
            iconColor = Color(0xFFF57F17),
            iconBgColor = Color(0xFFFFF8E1)
        )
    )

    private fun simuladorSteps() = listOf(
        TutorialStep(
            title = "Simulador financiero",
            description = "Proyecta diferentes escenarios para tu empresa: ¿qué pasa si aumentas tus cobros un 20%?",
            icon = Icons.Default.Calculate,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Variables ajustables",
            description = "Modifica ingresos estimados, egresos planificados y flujo de caja proyectado. El simulador recalcula todo en tiempo real.",
            icon = Icons.Default.Tune,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Sin efecto en datos reales",
            description = "Las simulaciones son solo proyecciones. No modifican ni afectan ningún documento, cheque o cuenta real de tu empresa.",
            icon = Icons.Default.Info,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        )
    )
}
