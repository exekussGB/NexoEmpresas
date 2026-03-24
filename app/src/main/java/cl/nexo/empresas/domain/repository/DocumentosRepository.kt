package cl.nexo.empresas.domain.repository

import cl.nexo.empresas.data.model.ChequeCreate
import cl.nexo.empresas.data.model.Documento
import cl.nexo.empresas.data.model.DocumentoCreate

interface DocumentosRepository {
    suspend fun getDocumentos(empresaId: String, tipo: String, estado: String? = null): Result<List<Documento>>
    suspend fun getDocumento(id: String): Result<Documento>
    suspend fun addDocumento(doc: DocumentoCreate, cheques: List<ChequeCreate> = emptyList()): Result<String>  // returns documento id
    suspend fun marcarPagado(id: String, fechaPago: String, numeroSeguimiento: String?): Result<Unit>
    suspend fun anularDocumento(id: String): Result<Unit>
}
