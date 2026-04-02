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
            description = "Tu herramienta para manejar las finanzas de tu negocio. Controla facturas, cheques, cuentas y más desde un solo lugar.",
            icon = Icons.Default.Business,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Tu empresa, tu espacio",
            description = "Cada empresa tiene su propia información separada. Puedes manejar varias empresas desde una sola cuenta.",
            icon = Icons.Default.Domain,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Trabajo en equipo",
            description = "Invita a tu contador, socio o equipo de trabajo. Cada persona tiene un rol con permisos diferentes según lo que necesite hacer.",
            icon = Icons.Default.Group,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        ),
        TutorialStep(
            title = "Comencemos",
            description = "Primero crea o únete a una empresa. Luego explora el panel principal con todas las secciones disponibles.",
            icon = Icons.Default.PlayArrow,
            iconColor = Color(0xFFF57F17),
            iconBgColor = Color(0xFFFFF8E1)
        )
    )

    private fun empresaSetupSteps() = listOf(
        TutorialStep(
            title = "Datos de tu empresa",
            description = "Ingresa el nombre y RUT de tu empresa. El RUT se ordena solo en el formato chileno (XX.XXX.XXX-X) y se verifica que sea correcto.",
            icon = Icons.Default.Edit,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Logo opcional",
            description = "Puedes subir el logo de tu empresa. Aparecerá en la parte superior de la aplicación.",
            icon = Icons.Default.Image,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Empresa creada",
            description = "¡Listo! Eres el Dueño de esta empresa. Solo tú puedes eliminarla o transferir la propiedad a otra persona.",
            icon = Icons.Default.CheckCircle,
            iconColor = Color(0xFF00695C),
            iconBgColor = Color(0xFFE0F7FA)
        ),
        TutorialStep(
            title = "Invita a tu equipo",
            description = "Invita a tu contador o colaboradores por correo electrónico desde Opciones. Luego puedes asignarles un rol en Miembros del Equipo.",
            icon = Icons.Default.PersonAdd,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        ),
        TutorialStep(
            title = "Múltiples empresas",
            description = "Si manejas más de una empresa, puedes crearlas todas desde Configuración. Cambia entre ellas con un solo toque.",
            icon = Icons.Default.SwapHoriz,
            iconColor = Color(0xFFF57F17),
            iconBgColor = Color(0xFFFFF8E1)
        )
    )

    private fun empresaMiembrosSteps() = listOf(
        TutorialStep(
            title = "Miembros del equipo",
            description = "Aquí ves a todas las personas de tu empresa y sus roles: Dueño, Administrador o Visualizador.",
            icon = Icons.Default.Group,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Roles y permisos",
            description = "El Dueño tiene control total. Los Administradores pueden agregar y modificar datos. Los Visualizadores solo pueden ver la información.",
            icon = Icons.Default.Security,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Código de invitación",
            description = "Comparte el código único de tu empresa para que nuevas personas se unan desde su propia cuenta.",
            icon = Icons.Default.Share,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        )
    )

    private fun hubSteps() = listOf(
        TutorialStep(
            title = "Panel Principal",
            description = "Desde aquí accedes a todas las secciones de tu empresa. El nombre de tu empresa aparece en la parte superior.",
            icon = Icons.Default.Home,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Secciones disponibles",
            description = "Cada botón te lleva a una parte diferente: documentos, cheques, cuentas, contactos, simulador y más.",
            icon = Icons.Default.Description,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Resumen y Gráficos",
            description = "Usa 'Resumen' para ver el estado financiero general y 'Opciones' para acceder a gráficos, alertas y guías de uso.",
            icon = Icons.Default.BarChart,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        ),
        TutorialStep(
            title = "Lector de facturas",
            description = "Lee facturas electrónicas usando el código de barras de la factura impresa. Extrae el RUT y el monto de forma automática.",
            icon = Icons.Default.QrCodeScanner,
            iconColor = Color(0xFFC62828),
            iconBgColor = Color(0xFFFFEBEE)
        )
    )

    // ════════════════════════════════════════
    // RESUMEN FINANCIERO
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
            title = "Cifras principales",
            description = "Los montos se actualizan solos. Cuando marcas un documento como pagado, los totales cambian de inmediato.",
            icon = Icons.Default.AttachMoney,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Datos siempre al día",
            description = "Los totales se calculan directamente desde tu información guardada en línea, para que siempre veas cifras exactas y actualizadas.",
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
            title = "Lectura rápida",
            description = "¿Tienes la factura impresa? Usa el botón de la cámara (📷) junto al número de factura para leer el código de barras de la factura y llenar los datos automáticamente.",
            icon = Icons.Default.QrCodeScanner,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        ),
        TutorialStep(
            title = "Categorías y notas",
            description = "Clasifica el documento con una categoría ya existente o crea una nueva. Agrega notas para recordar detalles importantes.",
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
            description = "Cuando pagues una factura, crea un registro de pago vinculado. El sistema marca el original como pagado de forma automática.",
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
            description = "Cuando un cliente te pague, crea un registro de cobro vinculado. El original se marcará como cobrado de forma automática.",
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
            description = "Registra cheques que recibes de clientes o que entregas a proveedores. Asocia cada cheque a un documento para llevar un mejor control.",
            icon = Icons.Default.Receipt,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Datos del cheque",
            description = "Ingresa número de cheque, banco, fecha de cobro o pago y monto. La fecha es importante para planificar tu flujo de dinero.",
            icon = Icons.Default.Edit,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Estados de cheque",
            description = "Un cheque puede estar: Pendiente → En cobro → Cobrado/Depositado o Rechazado. Actualiza el estado a medida que avanza el proceso.",
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
            description = "Crea una ficha por cada cliente o proveedor. Asocia documentos y cheques a tus contactos para tener un historial completo.",
            icon = Icons.Default.Contacts,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Verificación de RUT",
            description = "El RUT se ordena solo en el formato chileno (XX.XXX.XXX-X) y se verifica automáticamente que sea un RUT válido.",
            icon = Icons.Default.Verified,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Búsqueda rápida",
            description = "Busca por nombre o RUT desde la lista. El historial de documentos de cada contacto está disponible en su ficha.",
            icon = Icons.Default.Search,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        ),
        TutorialStep(
            title = "Tipo de contacto",
            description = "Clasifica cada contacto como cliente, proveedor o ambos. Así puedes organizar mejor tu red de negocios.",
            icon = Icons.Default.ContactPhone,
            iconColor = Color(0xFFF57F17),
            iconBgColor = Color(0xFFFFF8E1)
        )
    )

    private fun cuentasSteps() = listOf(
        TutorialStep(
            title = "Cuentas corrientes",
            description = "Registra las cuentas bancarias de tu empresa. Lleva el saldo actualizado y el detalle de cada movimiento.",
            icon = Icons.Default.AccountBalance,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Saldo actualizado",
            description = "El saldo se actualiza solo cuando registras depósitos o egresos asociados a la cuenta.",
            icon = Icons.Default.AttachMoney,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Varias cuentas",
            description = "Registra todas tus cuentas bancarias y lleva el control de cada una por separado. Así siempre sabrás cuánto tienes disponible.",
            icon = Icons.Default.CompareArrows,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        )
    )

    private fun graficosSteps() = listOf(
        TutorialStep(
            title = "Análisis visual",
            description = "Gráficos de barras y líneas para ver tu flujo de dinero, documentos por mes y cómo van tus ingresos comparados con tus gastos.",
            icon = Icons.Default.BarChart,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Filtros de período",
            description = "Selecciona el rango de fechas que quieres analizar. Los gráficos se actualizan de inmediato.",
            icon = Icons.Default.DateRange,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Exportar datos",
            description = "Próximamente podrás exportar los datos de los gráficos a un archivo para compartir con tu contador.",
            icon = Icons.Default.FileDownload,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        )
    )

    private fun alertasSteps() = listOf(
        TutorialStep(
            title = "Sistema de alertas",
            description = "Recibe avisos antes de que venzan tus documentos. Configura con cuántos días de anticipación quieres que te avisemos.",
            icon = Icons.Default.Notifications,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Revisión automática",
            description = "Las alertas se revisan periódicamente incluso cuando la aplicación está cerrada, para que no se te pase ningún vencimiento.",
            icon = Icons.Default.Schedule,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Avisos en tu celular",
            description = "Si tu celular tiene conexión a internet, recibirás un aviso directo cuando un documento esté por vencer.",
            icon = Icons.Default.PhoneAndroid,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        )
    )

    private fun scannerSteps() = listOf(
        TutorialStep(
            title = "Lector de facturas",
            description = "Lee facturas electrónicas impresas. Apunta la cámara al código de barras que aparece en la parte inferior de la factura.",
            icon = Icons.Default.QrCodeScanner,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Datos extraídos",
            description = "El lector obtiene automáticamente: tipo de documento, número de folio, RUT del que emite y del que recibe, monto neto, IVA y total.",
            icon = Icons.Default.DataObject,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Crear documento directo",
            description = "Una vez leída la factura, puedes crear un documento directamente con los datos obtenidos. Revisa y confirma antes de guardar.",
            icon = Icons.Default.NoteAdd,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        )
    )

    // ════════════════════════════════════════
    // SIMULADOR DE CONTRATACIÓN
    // ════════════════════════════════════════
    private fun simuladorSteps() = listOf(
        TutorialStep(
            title = "¿Cuánto cuesta contratar?",
            description = "Este simulador te muestra el costo total real de contratar a un trabajador. Ingresa el sueldo que pide el candidato y obtén el desglose completo.",
            icon = Icons.Default.Work,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            title = "Sueldo del candidato",
            description = "Ingresa el sueldo base que pide el candidato. El simulador calculará automáticamente todos los costos asociados.",
            icon = Icons.Default.MonetizationOn,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            title = "Ajusta las opciones",
            description = "Selecciona el tipo de contrato, la AFP, si usa Fonasa o Isapre, y el tipo de gratificación. Cada cambio recalcula el resultado al instante.",
            icon = Icons.Default.HealthAndSafety,
            iconColor = Color(0xFF6A1B9A),
            iconBgColor = Color(0xFFF3E5F5)
        ),
        TutorialStep(
            title = "Resultado completo",
            description = "Verás el costo total para tu empresa, el sueldo líquido del trabajador, los descuentos legales (AFP, salud, impuestos) y los costos adicionales del empleador (seguro, mutual, cesantía).",
            icon = Icons.Default.Calculate,
            iconColor = Color(0xFFF57F17),
            iconBgColor = Color(0xFFFFF8E1)
        )
    )
}
