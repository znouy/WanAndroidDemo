package com.example.wanandroiddemo.ui.common.delegate

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

/**
 * 契约的具体业务实现
 * 支持通过 Hilt 自动注入系统依赖（如 Context、网络配置等）
 */
class MessageDelegateImpl @Inject constructor() : MessageDelegate {

    private val _messageChannel = Channel<String>(Channel.BUFFERED)
    override val messageEvent: Flow<String> = _messageChannel.receiveAsFlow()

    override fun emitMessage(message: String) {
        _messageChannel.trySend(message)
    }
}
