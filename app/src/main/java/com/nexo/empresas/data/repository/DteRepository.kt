package com.nexo.empresas.dte.data.repository

import com.nexoempresas.dte.data.model.*
import com.nexoempresas.dte.data.remote.DteRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

@Singleton
class DteRepository @Inject constructor(
    private val remote: DteRemoteDataSource
) {

    // ── Emitir DTE ──────────────────────────────────────────────────────────

    fun emitirDte(request: EmitirDteRequest): Flow<Result<Dte>> = flow {
        emit(Result.Loading)
        runCatching { remote.emitirDte(request) }
            .onSuccess { response ->
                if (response.success && response.dte != null) {
                    emit(Result.Success(response.dte))
                } else {
                    emit(Result.Error(response.error ?: "Error desconocido al emitir DTE"))
                }
            }
            .onFailure { emit(Result.Error(it.message ?: "Error de red", it)) }
    }

    // ── Consultar estado SII ────────────────────────────────────────────────

    fun consultarEstado(dteId: String): Flow<Result<EstadoDteResponse>> = flow {
        emit(Result.Loading)
        runCatching { remote.consultarEstadoDte(dteId) }
            .onSuccess { emit(Result.Success(it)) }
            .onFailure { emit(Result.Error(it.message ?: "Error al consultar estado", it)) }
    }

    // ── Listar DTEs ─────────────────────────────────────────────────────────

    fun listarDtes(
        empresaId: String,
        estadoFiltro: String? = null,
        limit: Int = 50,
        offset: Int = 0
    ): Flow<Result<List<Dte>>> = flow {
        emit(Result.Loading)
        runCatching { remote.listarDtes(empresaId, estadoFiltro, limit, offset) }
            .onSuccess { emit(Result.Success(it)) }
            .onFailure { emit(Result.Error(it.message ?: "Error al listar DTEs", it)) }
    }

    // ── Obtener DTE ─────────────────────────────────────────────────────────

    fun obtenerDte(dteId: String): Flow<Result<Dte>> = flow {
        emit(Result.Loading)
        runCatching {
            val dte = remote.obtenerDte(dteId)
            val items = remote.obtenerItemsDte(dteId)
            dte.copy(items = items)
        }
            .onSuccess { emit(Result.Success(it)) }
            .onFailure { emit(Result.Error(it.message ?: "Error al obtener DTE", it)) }
    }

    // ── Folios ──────────────────────────────────────────────────────────────

    fun listarFolios(empresaId: String): Flow<Result<List<Folio>>> = flow {
        emit(Result.Loading)
        runCatching { remote.listarFolios(empresaId) }
            .onSuccess { emit(Result.Success(it)) }
            .onFailure { emit(Result.Error(it.message ?: "Error al obtener folios", it)) }
    }

    // ── Lookup RUT ──────────────────────────────────────────────────────────

    fun lookupRut(rut: String): Flow<Result<RutInfo>> = flow {
        emit(Result.Loading)
        runCatching { remote.lookupRut(rut) }
            .onSuccess { emit(Result.Success(it)) }
            .onFailure { emit(Result.Error(it.message ?: "RUT no encontrado", it)) }
    }

    // ── PDF / XML ───────────────────────────────────────────────────────────

    suspend fun getPdfUrl(pdfPath: String): String = remote.descargarPdfUrl(pdfPath)
    suspend fun getXmlUrl(xmlPath: String): String = remote.descargarXmlUrl(xmlPath)

    // ── Registrar certificado ───────────────────────────────────────────────

    fun registrarCertificado(
        empresaId: String,
        pfxBase64: String,
        clavePfx: String
    ): Flow<Result<Boolean>> = flow {
        emit(Result.Loading)
        runCatching { remote.registrarCertificado(empresaId, pfxBase64, clavePfx) }
            .onSuccess { emit(Result.Success(it)) }
            .onFailure { emit(Result.Error(it.message ?: "Error al registrar certificado", it)) }
    }
}
