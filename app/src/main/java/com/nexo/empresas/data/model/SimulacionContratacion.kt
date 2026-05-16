package com.nexo.empresas.data.model

/**
 * Tasas y valores vigentes para cálculo de remuneraciones Chile.
 * Actualizar periódicamente según publicación del SII y Superintendencia de Pensiones.
 */
object RemuneracionesChile {

    // ── Vigencia de parámetros ──
    const val VIGENCIA_DESDE_FALLBACK = "Enero 2026"
    var vigenciaDesde: String = "enero 2026"

    // ── UTM e Indicadores (Marzo 2026) ──
    var UTM = 69_889L                      // Valor UTM marzo 2026 (actualizable por API)
    var isUtmUpdated = false               // Indica si se obtuvo valor real de API

    const val UF = 38_876.52                 // Referencial
    const val INGRESO_MINIMO = 539_000L      // Ley 21.751, vigente desde enero 2026
    // IMM: update manually when Ley reajuste is published (usually July)

    const val TOPE_IMPONIBLE_UF = 81.6       // 81,6 UF
    val TOPE_IMPONIBLE: Long get() = (TOPE_IMPONIBLE_UF * UF).toLong() // ~$3.173.208

    // ── AFP — Comisiones vigentes 2025-2026 ──
    data class Afp(val nombre: String, val comision: Double, val total: Double) {
        // total = 10% obligatorio + comisión
    }

    val AFP_LIST = listOf(
        Afp("Capital",     1.44, 11.44),
        Afp("Cuprum",      1.44, 11.44),
        Afp("Habitat",     1.27, 11.27),
        Afp("Modelo",      0.58, 10.58),
        Afp("PlanVital",   1.16, 11.16),
        Afp("ProVida",     1.45, 11.45),
        Afp("Uno",         0.49, 10.49),
    )

    // ── Salud ──
    const val FONASA_TASA = 0.07            // 7%

    // ── Seguro de Cesantía ──
    const val CESANTIA_TRABAJADOR_INDEFINIDO = 0.006  // 0,6%
    const val CESANTIA_EMPLEADOR_INDEFINIDO  = 0.024  // 2,4%
    const val CESANTIA_EMPLEADOR_PLAZO_FIJO  = 0.03   // 3,0% (paga solo empleador)

    // ── Seguro Invalidez y Sobrevivencia (SIS) — cargo empleador ──
    const val SIS_TASA = 0.0154             // 1,54%

    // ── Mutual de Seguridad — cargo empleador ──
    const val MUTUAL_BASE = 0.0093          // 0,93% base
    // Tasa adicional según actividad económica: 0% a 3,4%

    // ── Gratificación Legal ──
    const val TOPE_GRATIFICACION_ART50 = 4.75 // 4,75 IMM (Ingreso Mínimo Mensual)
    const val TOPE_GRATIFICACION_MENSUAL = 213_354L // (4.75 * IMM / 12) aprox

    // ── Impuesto Único Segunda Categoría (IUSC) — Tramos mensuales en UTM ──
    data class TramoImpuesto(
        val desde: Double,    // en UTM
        val hasta: Double,    // en UTM (Double.MAX_VALUE = sin tope)
        val tasa: Double,     // tasa marginal
        val rebaja: Double    // factor de rebaja en UTM
    )

    val TRAMOS_IUSC = listOf(
        TramoImpuesto(  0.0,   13.5,  0.00,   0.000),
        TramoImpuesto( 13.5,   30.0,  0.04,   0.540),
        TramoImpuesto( 30.0,   50.0,  0.08,   1.740),
        TramoImpuesto( 50.0,   70.0,  0.135,  4.490),
        TramoImpuesto( 70.0,   90.0,  0.23,  11.140),
        TramoImpuesto( 90.0,  120.0,  0.304, 17.800),
        TramoImpuesto(120.0,  310.0,  0.35,  23.320),
        TramoImpuesto(310.0, Double.MAX_VALUE, 0.40, 38.820),
    )
}

/**
 * Resultado completo de la simulación de costo de contratación.
 */
data class SimulacionResult(
    // ── Haberes ──
    val sueldoBase: Long,
    val gratificacion: Long,
    val comisiones: Long,
    val bonosImponibles: Long,
    val horasExtras: Long,
    val totalImponible: Long,
    val imponibleTopado: Long,          // min(totalImponible, tope)
    val excedeTopeImponible: Boolean,

    val colacion: Long,
    val movilizacion: Long,
    val viaticos: Long,
    val desgasteHerramientas: Long,
    val bonosNoImponibles: Long,
    val totalNoImponible: Long,

    val totalHaberes: Long,             // imponible + no imponible

    // ── Descuentos trabajador ──
    val afpNombre: String,
    val afpMonto: Long,                 // sobre imponible topado
    val saludMonto: Long,               // 7% Fonasa o cotización Isapre
    val saludDetalle: String,           // "Fonasa 7%" o "Isapre $XX.XXX"
    val cesantiaTrabajador: Long,       // 0.6% indefinido, 0 plazo fijo
    val baseImponibleImpuesto: Long,    // imponibleTopado - AFP - salud - cesantía
    val impuestoUnico: Long,            // IUSC
    val tasaEfectivaImpuesto: Double,   // % efectivo
    val anticipo: Long,
    val prestamoEmpresa: Long,
    val otrosDescuentos: Long,
    val otrosDescuentosLabel: String,
    val totalDescuentosTrabajador: Long,

    // ── Líquido ──
    val sueldoLiquido: Long,            // totalHaberes - totalDescuentos
    val sueldoLiquidoDeseado: Long? = null,

    // ── Costos empleador ──
    val sisMonto: Long,                 // 1,54%
    val cesantiaEmpleador: Long,        // 2,4% o 3,0%
    val mutualMonto: Long,              // 0,93% + adicional
    val totalCostosEmpleador: Long,

    // ── COSTO TOTAL ──
    val costoTotalEmpresa: Long,        // totalHaberes + totalCostosEmpleador
)

/**
 * Input para la simulación.
 */
data class SimulacionInput(
    val nombreCandidato: String = "",
    val modoCalculo: ModoCalculo = ModoCalculo.DESDE_BRUTO,
    val sueldoBase: Long = 0,
    val sueldoLiquidoDeseado: Long = 0,
    val gratificacionTipo: GratificacionTipo = GratificacionTipo.ART_50,
    val comisiones: Long = 0,
    val bonosImponibles: Long = 0,
    val horasExtraCount: Int = 0,
    val colacion: Long = 0,
    val movilizacion: Long = 0,
    val viaticos: Long = 0,
    val desgasteHerramientas: Long = 0,
    val bonosNoImponibles: Long = 0,
    val anticipo: Long = 0,
    val prestamoEmpresa: Long = 0,
    val otrosDescuentos: Long = 0,
    val otrosDescuentosLabel: String = "Otros descuentos",
    val tipoContrato: TipoContrato = TipoContrato.INDEFINIDO,
    val afpIndex: Int = 0,
    val tipoSalud: TipoSalud = TipoSalud.FONASA,
    val cotizacionIsapre: Long = 0,     // monto fijo en pesos si es Isapre
    val tasaMutualAdicional: Double = 0.0,
    val colacionManual: Boolean = false,
    val movilizacionManual: Boolean = false,
    val viaticosManual: Boolean = false,
    val desgasteHerramientasManual: Boolean = false,
    val otrosNoImponiblesManual: Boolean = false,
    val warningPorDebajoMinimo: Boolean = false,
    val errorSueldoExcedido: Boolean = false,
)

enum class GratificacionTipo(val label: String) {
    ART_50("Art. 50 (25% con tope)"),
    ART_47("Art. 47 (30% utilidades)"),
    SIN_GRATIFICACION("Sin gratificación"),
}

enum class TipoContrato(val label: String) {
    INDEFINIDO("Indefinido"),
    PLAZO_FIJO("Plazo Fijo"),
    OBRA_FAENA("Obra o Faena"),
}

enum class TipoSalud(val label: String) {
    FONASA("Fonasa (7%)"),
    ISAPRE("Isapre"),
}

enum class ModoCalculo(val label: String) {
    DESDE_BRUTO("Sueldo Bruto"),
    DESDE_LIQUIDO("Sueldo Líquido"),
}

data class ComparacionEscenarios(
    val costoTodoImponible: Long,
    val costoSugerido: Long
) {
    val ahorroMensual = costoTodoImponible - costoSugerido
    val ahorroAnual = ahorroMensual * 12
}
