package cl.nexo.empresas.core.di

import cl.nexo.empresas.data.repository.*
import cl.nexo.empresas.domain.repository.*
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
    @Binds @Singleton abstract fun bindDocumentosRepository(impl: DocumentosRepositoryImpl): DocumentosRepository
    @Binds @Singleton abstract fun bindChequesRepository(impl: ChequesRepositoryImpl): ChequesRepository
}
