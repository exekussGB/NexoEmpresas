package com.nexo.empresas.domain.repository

import com.nexo.empresas.data.model.ChequeCreate
import com.nexo.empresas.data.model.Documento
import com.nexo.empresas.data.model.DocumentoCreate

interface DocumentosRepository {
    suspend fun getDocumentos(empresaId: String, tipo: String, estado: String? = null): Result<List<Documento>>
    suspend fun getDocumento(id: String): Result<Documento>
    suspend fun addDocumento(doc: DocumentoCreate, cheques: List<ChequeCreate> = emptyList()): Result<String>
    suspend fun marcarPagado(id: String, fechaPago: String, numeroSeguimiento: String?): Result<Unit>
    suspend fun anularDocumento(id: String): Result<Unit>
    // Usado por VencimientosCheckWorker — tipo: "ingreso"|"egreso", fechaLimite: "YYYY-MM-DD"
    suspend fun getDocumentosVencimientoProximo(tipo: String, fechaLimite: String): Result<List<Documento>>
}
