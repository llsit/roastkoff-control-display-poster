package com.roastkoff.controlposter.di

import com.roastkoff.controlposter.data.AuthRepository
import com.roastkoff.controlposter.data.AuthRepositoryImpl
import com.roastkoff.controlposter.data.DashRepository
import com.roastkoff.controlposter.data.DashRepositoryImpl
import com.roastkoff.controlposter.data.DisplayRepository
import com.roastkoff.controlposter.data.DisplayRepositoryImpl
import com.roastkoff.controlposter.data.GroupRepository
import com.roastkoff.controlposter.data.GroupRepositoryImpl
import com.roastkoff.controlposter.data.PlaylistRepository
import com.roastkoff.controlposter.data.PlaylistRepositoryImpl
import com.roastkoff.controlposter.data.UserRepository
import com.roastkoff.controlposter.data.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
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
    abstract fun bindBranchRepository(groupRepository: GroupRepositoryImpl): GroupRepository

    @Binds
    abstract fun bindDashRepositoryImpl(dashRepository: DashRepositoryImpl): DashRepository

    @Binds
    abstract fun bindDisplayRepositoryImpl(displayRepository: DisplayRepositoryImpl): DisplayRepository

    @Binds
    abstract fun bindPlaylistRepository(playlistRepository: PlaylistRepositoryImpl): PlaylistRepository
}