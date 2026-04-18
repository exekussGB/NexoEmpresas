package com.nexo.empresas

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Punto de entrada principal de la aplicación Android.
 * Inicializa Hilt para la inyección de dependencias y configura servicios globales.
 */
@HiltAndroidApp
class NexoEmpresasApp : Application()
