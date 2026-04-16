package com.nexo.empresas.dte.data.repository

/**
 * Sealed class para manejar resultados con Flow en el ViewModel
 * Reemplaza a Resource para ser compatible con coroutines
 */
sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val message: String) : Result<T>()
    class Loading<T> : Result<T>()
}
