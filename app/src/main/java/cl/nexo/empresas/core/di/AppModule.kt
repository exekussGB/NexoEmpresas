package cl.nexo.empresas.core.di

import cl.nexo.empresas.data.repository.AuthRepositoryImpl
import cl.nexo.empresas.data.repository.ContactosRepositoryImpl
import cl.nexo.empresas.data.repository.CuentasCorrientesRepositoryImpl
import cl.nexo.empresas.data.repository.EmpresasRepositoryImpl
import cl.nexo.empresas.data.repository.GraficosRepositoryImpl
import cl.nexo.empresas.domain.repository.AuthRepository
import cl.nexo.empresas.domain.repository.ContactosRepository
import cl.nexo.empresas.domain.repository.CuentasCorrientesRepository
import cl.nexo.empresas.domain.repository.EmpresasRepository
import cl.nexo.empresas.domain.repository.GraficosRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds @Singleton abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun bindEmpresasRepository(impl: EmpresasRepositoryImpl): EmpresasRepository
    @Binds @Singleton abstract fun bindContactosRepository(impl: ContactosRepositoryImpl): ContactosRepository
    @Binds @Singleton abstract fun bindCuentasCorrientesRepository(impl: CuentasCorrientesRepositoryImpl): CuentasCorrientesRepository
    @Binds @Singleton abstract fun bindGraficosRepository(impl: GraficosRepositoryImpl): GraficosRepository
}
