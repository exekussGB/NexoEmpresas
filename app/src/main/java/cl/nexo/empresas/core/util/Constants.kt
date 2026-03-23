package cl.nexo.empresas.core.util

import cl.nexo.empresas.BuildConfig

object Constants {
    const val SUPABASE_URL = BuildConfig.SUPABASE_URL
    const val SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY
    const val TABLE_EMPRESAS = "empresas"
    const val TABLE_EMPRESA_MEMBERS = "empresa_members"
    const val TABLE_CONTACTOS = "contactos"
    const val TABLE_CUENTAS_CORRIENTES = "cuentas_corrientes"
    const val TABLE_DOCUMENTOS = "documentos"
    const val TABLE_CHEQUES = "cheques"
    const val TABLE_ALERTAS_CONFIG = "alertas_config"
    const val STORAGE_LOGOS_BUCKET = "logos"
}
