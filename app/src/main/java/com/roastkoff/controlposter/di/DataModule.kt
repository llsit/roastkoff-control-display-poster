package com.roastkoff.controlposter.di

import com.roastkoff.controlposter.data.AuthRepository
import com.roastkoff.controlposter.data.AuthRepositoryImpl
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
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}