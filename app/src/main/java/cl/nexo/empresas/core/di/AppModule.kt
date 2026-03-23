package cl.nexo.empresas.core.di

import cl.nexo.empresas.data.repository.AuthRepositoryImpl
import cl.nexo.empresas.data.repository.EmpresasRepositoryImpl
import cl.nexo.empresas.domain.repository.AuthRepository
import cl.nexo.empresas.domain.repository.EmpresasRepository
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
}
