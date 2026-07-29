package com.example.wanandroiddemo.di

import com.example.wanandroiddemo.ui.common.delegate.CollectDelegate
import com.example.wanandroiddemo.ui.common.delegate.CollectDelegateImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class DelegateModule {

    @Binds
    @ViewModelScoped
    abstract fun bindCollectDelegate(
        impl: CollectDelegateImpl
    ): CollectDelegate
}