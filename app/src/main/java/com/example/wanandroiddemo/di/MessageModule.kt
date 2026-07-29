package com.example.wanandroiddemo.di
import com.example.wanandroiddemo.ui.common.delegate.MessageDelegate
import com.example.wanandroiddemo.ui.common.delegate.MessageDelegateImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class) // 生命周期与 ViewModel 同步
abstract class MessageModule {

    @Binds
    @ViewModelScoped // 保证在单个 ViewModel 实例内保持单例
    abstract fun bindMessageDelegate(
        impl: MessageDelegateImpl
    ): MessageDelegate
}

