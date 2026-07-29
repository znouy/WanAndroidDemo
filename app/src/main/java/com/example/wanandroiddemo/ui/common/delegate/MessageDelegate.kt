package com.example.wanandroiddemo.ui.common.delegate

import kotlinx.coroutines.flow.Flow

/**
 * 业务层消息契约：只定义“发送消息”和“消息数据流”，不含任何平台 UI 痕迹
 */
interface MessageDelegate {
    val messageEvent: Flow<String>
    fun emitMessage(message: String)
}