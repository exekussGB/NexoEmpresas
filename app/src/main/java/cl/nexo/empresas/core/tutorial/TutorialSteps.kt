package cl.nexo.empresas.core.tutorial

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
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonSearch
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
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color

object TutorialSteps {

    fun getSteps(module: TutorialModule): List<TutorialStep> = when (module) {
        TutorialModule.ONBOARDING -> onboardingSteps()
        TutorialModule.EMPRESA_SETUP -> empresaSetupSteps()
        TutorialModule.EMPRESA_MIEMBROS -> empresaMiembrosSteps()
        TutorialModule.HUB -> hubSteps()
        TutorialModule.RESUMEN -> resumenSteps()
        TutorialModule.INGRESAR_DOC -> ingresarDocSteps()
        TutorialModule.POR_PAGAR -> porPagarSteps()
        TutorialModule.POR_COBRAR -> porCobrarSteps()
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
            title = "Módulos disponibles",
            description = "Cada botón te lleva a una sección diferente: documentos, cheques, cuentas, contactos, simulador y más.",
            icon = Icons.Default.Description,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Resumen y Gráficos",
            description = "Usa 'Resumen' para ver el estado financiero general y 'Opciones' para acceder a gráficos, alertas y tutoriales.",
            icon = Icons.Default.BarChart,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        ),
        TutorialStep(
            title = "Escáner DTE",
            description = "Lee documentos tributarios electrónicos usando el código PDF417 del papel. Extrae el RUT y monto automáticamente.",
            icon = Icons.Default.QrCodeScanner,
            iconColor = Color(0xFFC62828),
            iconBgColor = Color(0xFFFFEBEE)
        )
    )

    // ════════════════════════════════════════
    // RESUMEN FINANCIERO (ex-Dashboard)
    // ════════════════════════════════════════
    private fun resumenSteps() = listOf(
        TutorialStep(
            title = "Resumen Financiero",
            description = "Aquí ves un panorama completo de tu empresa: total por cobrar, total por pagar, saldo de cuentas y movimientos del mes.",
            icon = Icons.Default.TrendingUp,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Indicadores principales",
            description = "Los montos se actualizan automáticamente. Cuando marcas un documento como pagado, se refleja al instante en los totales.",
            icon = Icons.Default.AttachMoney,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Datos siempre al día",
            description = "Los totales se calculan directamente desde tus datos en la nube, asegurando que siempre veas cifras exactas y actualizadas.",
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

    // ════════════════════════════════════════
    // INGRESAR DOCUMENTO
    // ════════════════════════════════════════
    private fun ingresarDocSteps() = listOf(
        TutorialStep(
            title = "Ingresar Documento",
            description = "Aquí registras nuevas facturas, boletas o notas de crédito. Elige si es un ingreso (te deben a ti) o un egreso (tú debes).",
            icon = Icons.Default.NoteAdd,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Datos del documento",
            description = "Completa el número de factura, monto, fecha de vencimiento y contacto asociado. Los campos con * son obligatorios.",
            icon = Icons.Default.Edit,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Escaneo rápido",
            description = "¿Tienes la factura impresa? Usa el botón del escáner (📷) junto al número de factura para leer el código PDF417 y llenar los datos automáticamente.",
            icon = Icons.Default.QrCodeScanner,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        ),
        TutorialStep(
            title = "Categorías y notas",
            description = "Clasifica el documento con una categoría predefinida o crea una personalizada. Agrega notas para recordar detalles importantes.",
            icon = Icons.Default.Category,
            iconColor = Color(0xFFF57F17),
            iconBgColor = Color(0xFFFFF8E1)
        )
    )

    // ════════════════════════════════════════
    // POR PAGAR
    // ════════════════════════════════════════
    private fun porPagarSteps() = listOf(
        TutorialStep(
            title = "Documentos Por Pagar",
            description = "Aquí están todas las facturas y cuentas que tu empresa debe pagar. Se ordenan por fecha de vencimiento.",
            icon = Icons.Default.TrendingDown,
            iconColor = Color(0xFFC62828),
            iconBgColor = Color(0xFFFFEBEE)
        ),
        TutorialStep(
            title = "Estados del documento",
            description = "Cada documento puede estar Pendiente, Pagado o Vencido. Los vencidos se marcan en rojo para que no se te pasen.",
            icon = Icons.Default.Schedule,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Registrar pago",
            description = "Cuando pagues una factura, crea un documento de pago vinculado. El sistema marca el original como pagado automáticamente.",
            icon = Icons.Default.Payment,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        ),
        TutorialStep(
            title = "Filtrar y buscar",
            description = "Filtra por estado (pendiente, pagado, vencido), rango de fechas o contacto para encontrar cualquier documento rápidamente.",
            icon = Icons.Default.FilterList,
            iconColor = Color(0xFFF57F17),
            iconBgColor = Color(0xFFFFF8E1)
        )
    )

    // ════════════════════════════════════════
    // POR COBRAR
    // ════════════════════════════════════════
    private fun porCobrarSteps() = listOf(
        TutorialStep(
            title = "Documentos Por Cobrar",
            description = "Aquí están todas las facturas que te deben a ti. Lleva el control de tus ingresos pendientes.",
            icon = Icons.Default.TrendingUp,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Seguimiento de cobros",
            description = "Revisa qué clientes tienen facturas pendientes y cuáles están por vencer. Los documentos vencidos se destacan en rojo.",
            icon = Icons.Default.PersonSearch,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Registrar cobro",
            description = "Cuando un cliente te pague, crea un documento de cobro vinculado. El original se marcará como cobrado automáticamente.",
            icon = Icons.Default.AttachMoney,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        ),
        TutorialStep(
            title = "Filtrar y buscar",
            description = "Usa los filtros por estado, fecha o contacto para encontrar rápidamente lo que necesitas.",
            icon = Icons.Default.FilterList,
            iconColor = Color(0xFFF57F17),
            iconBgColor = Color(0xFFFFF8E1)
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
            description = "Ingresa número de cheque, banco, fecha de cobro/pago y monto. La fecha es clave para proyectar tu flujo de caja.",
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
            title = "Orden por vencimiento",
            description = "Los cheques se ordenan por fecha para que siempre veas primero los que vencen antes.",
            icon = Icons.Default.Sort,
            iconColor = Color(0xFFF57F17),
            iconBgColor = Color(0xFFFFF8E1)
        )
    )

    private fun contactosSteps() = listOf(
        TutorialStep(
            title = "Clientes y proveedores",
            description = "Crea una ficha por cada cliente o proveedor. Asocia documentos y cheques a sus contactos para tener un historial completo.",
            icon = Icons.Default.Contacts,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Validación de RUT",
            description = "El RUT se formatea automáticamente (XX.XXX.XXX-X) y se valida con el algoritmo oficial chileno.",
            icon = Icons.Default.Verified,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Búsqueda rápida",
            description = "Busca por nombre o RUT desde la lista. El historial de documentos de cada contacto está disponible en su perfil.",
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
            title = "Saldo actualizado",
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
            title = "Verificación automática",
            description = "Las alertas se revisan periódicamente incluso cuando la app está cerrada, para que no se te pase ningún vencimiento.",
            icon = Icons.Default.Schedule,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Notificaciones en tu celular",
            description = "Si tu dispositivo tiene internet, recibirás una notificación directa cuando un documento esté por vencer.",
            icon = Icons.Default.PhoneAndroid,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        )
    )

    private fun scannerSteps() = listOf(
        TutorialStep(
            title = "Escáner de documentos",
            description = "Lee documentos tributarios electrónicos. Apunta la cámara al código de barras que aparece en la parte inferior de la factura impresa.",
            icon = Icons.Default.QrCodeScanner,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Datos extraídos",
            description = "El escáner extrae automáticamente: tipo de documento, número de folio, RUT del emisor y receptor, monto neto, IVA y total.",
            icon = Icons.Default.DataObject,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Crear documento directo",
            description = "Una vez escaneado, puedes crear un documento directamente con los datos extraídos. Revisa y confirma antes de guardar.",
            icon = Icons.Default.NoteAdd,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        )
    )

    // ════════════════════════════════════════
    // SIMULADOR DE CONTRATACIÓN (reescrito)
    // ════════════════════════════════════════
    private fun simuladorSteps() = listOf(
        TutorialStep(
            title = "¿Cuánto cuesta contratar?",
            description = "Este simulador te muestra el costo total real de contratar a un trabajador. Ingresa las pretensiones de renta del candidato y obtén el desglose completo.",
            icon = Icons.Default.Work,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Pretensión de renta",
            description = "Ingresa el sueldo base que pide el candidato. El simulador calculará automáticamente todos los costos asociados.",
            icon = Icons.Default.MonetizationOn,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Ajusta los parámetros",
            description = "Selecciona el tipo de contrato, la AFP, si usa Fonasa o Isapre, y el tipo de gratificación. Cada cambio recalcula el resultado al instante.",
            icon = Icons.Default.HealthAndSafety,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        ),
        TutorialStep(
            title = "Resultado completo",
            description = "Verás el costo total para tu empresa, el sueldo líquido del trabajador, los descuentos legales (AFP, salud, impuestos) y los costos adicionales del empleador (SIS, mutual, cesantía).",
            icon = Icons.Default.Calculate,
            iconColor = Color(0xFFF57F17),
            iconBgColor = Color(0xFFFFF8E1)
        )
    )
}
