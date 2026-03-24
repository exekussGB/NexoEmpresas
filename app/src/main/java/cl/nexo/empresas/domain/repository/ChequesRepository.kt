package cl.nexo.empresas.domain.repository

import cl.nexo.empresas.data.model.Cheque

interface ChequesRepository {
    suspend fun getCheques(empresaId: String, estado: String? = null): Result<List<Cheque>>
    suspend fun getChequesDeDocumento(documentoId: String): Result<List<Cheque>>
    suspend fun actualizarEstadoCheque(chequeId: String, estado: String): Result<Unit>
    // Usado por VencimientosCheckWorker — fechaLimite: "YYYY-MM-DD"
    suspend fun getChequesVencimientoProximo(fechaLimite: String): Result<List<Cheque>>
}
