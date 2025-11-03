package com.roastkoff.controlposter.di

import com.roastkoff.controlposter.data.AuthRepository
import com.roastkoff.controlposter.data.AuthRepositoryImpl
import com.roastkoff.controlposter.data.BranchRepository
import com.roastkoff.controlposter.data.BranchRepositoryImpl
import com.roastkoff.controlposter.data.DashRepository
import com.roastkoff.controlposter.data.DashRepositoryImpl
import com.roastkoff.controlposter.data.UserRepository
import com.roastkoff.controlposter.data.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindAuthRepository(authRepository: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindUserRepository(userRepository: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun bindBranchRepository(branchRepository: BranchRepositoryImpl): BranchRepository

    @Binds
    abstract fun bindDashRepositoryImpl(dashRepository: DashRepositoryImpl): DashRepository
}