package com.nexo.empresas.data.repository

import com.nexo.empresas.data.model.Dte
import com.nexo.empresas.data.model.EmitirDteRequest
import com.nexo.empresas.data.model.EmitirDteResponse
import com.nexo.empresas.data.model.EstadoDteResponse
import com.nexo.empresas.data.model.Folio
import com.nexo.empresas.data.model.ItemDte
import com.nexo.empresas.data.model.RutInfo
import com.nexo.empresas.data.remote.dte.DteRemoteDataSource

sealed class Resource<T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val exception: Exception) : Resource<T>()
    class Loading<T> : Resource<T>()
}

class DteRepository(
    private val remoteDataSource: DteRemoteDataSource
) {

    /**
     * Emite un nuevo DTE y retorna el resultado envuelto en Resource
     */
    suspend fun emitirDte(request: EmitirDteRequest): Resource<EmitirDteResponse> {
        return remoteDataSource.emitirDte(request)
            .mapCatching { response ->
                Resource.Success(response)
            }
            .getOrElse { exception ->
                Resource.Error(exception as? Exception ?: Exception(exception.message))
            }
    }

    /**
     * Obtiene el estado de un DTE
     */
    suspend fun obtenerEstadoDte(dteId: String): Resource<EstadoDteResponse> {
        return remoteDataSource.obtenerEstadoDte(dteId)
            .mapCatching { response ->
                Resource.Success(response)
            }
            .getOrElse { exception ->
                Resource.Error(exception as? Exception ?: Exception(exception.message))
            }
    }

    /**
     * Obtiene información del RUT
     */
    suspend fun obtenerInfoRut(rut: String): Resource<RutInfo> {
        return remoteDataSource.obtenerInfoRut(rut)
            .mapCatching { response ->
                Resource.Success(response)
            }
            .getOrElse { exception ->
                Resource.Error(exception as? Exception ?: Exception(exception.message))
            }
    }

    /**
     * Obtiene los folios disponibles para una empresa
     */
    suspend fun obtenerFolios(rutEmpresa: String): Resource<List<Folio>> {
        return remoteDataSource.obtenerFolios(rutEmpresa)
            .mapCatching { response ->
                Resource.Success(response)
            }
            .getOrElse { exception ->
                Resource.Error(exception as? Exception ?: Exception(exception.message))
            }
    }

    /**
     * Obtiene los DTEs de una empresa
     */
    suspend fun obtenerDtesPorEmpresa(rutEmpresa: String): Resource<List<Dte>> {
        return remoteDataSource.obtenerDtesPorEmpresa(rutEmpresa)
            .mapCatching { response ->
                Resource.Success(response)
            }
            .getOrElse { exception ->
                Resource.Error(exception as? Exception ?: Exception(exception.message))
            }
    }

    /**
     * Obtiene un DTE específico
     */
    suspend fun obtenerDte(dteId: String): Resource<Dte> {
        return remoteDataSource.obtenerDte(dteId)
            .mapCatching { response ->
                Resource.Success(response)
            }
            .getOrElse { exception ->
                Resource.Error(exception as? Exception ?: Exception(exception.message))
            }
    }

    /**
     * Obtiene los items de un DTE
     */
    suspend fun obtenerItemsDte(dteId: String): Resource<List<ItemDte>> {
        return remoteDataSource.obtenerItemsDte(dteId)
            .mapCatching { response ->
                Resource.Success(response)
            }
            .getOrElse { exception ->
                Resource.Error(exception as? Exception ?: Exception(exception.message))
            }
    }

    /**
     * Obtiene el folio actual para un tipo de DTE
     */
    suspend fun obtenerFolioActual(
        rutEmpresa: String,
        tipoDte: Int
    ): Resource<Folio> {
        return remoteDataSource.obtenerFolioActual(rutEmpresa, tipoDte)
            .mapCatching { response ->
                Resource.Success(response)
            }
            .getOrElse { exception ->
                Resource.Error(exception as? Exception ?: Exception(exception.message))
            }
    }
}
