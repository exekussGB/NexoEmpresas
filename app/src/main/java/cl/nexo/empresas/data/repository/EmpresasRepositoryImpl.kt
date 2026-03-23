package cl.nexo.empresas.data.repository

import cl.nexo.empresas.core.util.Constants
import cl.nexo.empresas.data.model.CreateEmpresaRequest
import cl.nexo.empresas.data.model.Empresa
import cl.nexo.empresas.data.model.EmpresaMember
import cl.nexo.empresas.domain.repository.EmpresasRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject

class EmpresasRepositoryImpl @Inject constructor(
    private val client: SupabaseClient
) : EmpresasRepository {

    override suspend fun getEmpresasForUser(): Result<List<Empresa>> = runCatching {
        client.from(Constants.TABLE_EMPRESAS).select().decodeList<Empresa>()
    }

    override suspend fun createEmpresa(empresa: Empresa): Result<Empresa> = runCatching {
        val request = CreateEmpresaRequest(
            nombre = empresa.nombre,
            rut = empresa.rut,
            giro = empresa.giro,
            createdBy = empresa.createdBy!!
        )
        client.from(Constants.TABLE_EMPRESAS)
            .insert(request) { select() }
            .decodeSingle<Empresa>()
    }

    override suspend fun joinByCode(inviteCode: String): Result<Unit> = runCatching {
        val empresa = client.from(Constants.TABLE_EMPRESAS)
            .select { filter { eq("invite_code", inviteCode) } }
            .decodeSingle<Empresa>()
        val member = EmpresaMember(empresaId = empresa.id, rol = "viewer")
        client.from(Constants.TABLE_EMPRESA_MEMBERS).insert(member)
    }

    override suspend fun getMemberRole(empresaId: String, userId: String): Result<String> = runCatching {
        client.from(Constants.TABLE_EMPRESA_MEMBERS)
            .select { filter { eq("empresa_id", empresaId); eq("user_id", userId) } }
            .decodeSingle<EmpresaMember>().rol
    }
}