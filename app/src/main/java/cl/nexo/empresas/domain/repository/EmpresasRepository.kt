package cl.nexo.empresas.domain.repository

import cl.nexo.empresas.data.model.Empresa
import cl.nexo.empresas.data.model.EmpresaMember

interface EmpresasRepository {
    suspend fun getEmpresasForUser(): Result<List<Empresa>>
    suspend fun createEmpresa(empresa: Empresa): Result<Empresa>
    suspend fun joinByCode(inviteCode: String): Result<Unit>
    suspend fun getMemberRole(empresaId: String, userId: String): Result<String>
}
