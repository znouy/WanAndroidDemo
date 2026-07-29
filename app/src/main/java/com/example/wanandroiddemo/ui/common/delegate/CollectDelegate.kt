package com.example.wanandroiddemo.ui.common.delegate

import com.example.wanandroiddemo.data.model.domain.Article
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * 收藏功能插件化接口
 */
interface CollectDelegate {
    // 观察本地修改暂存区
    val localModifications: StateFlow<Map<Int, Boolean>>

    // 统一的用户提示通道,使用 Flow 承载来自 Channel 的单次事件
    val collectUiEvent: Flow<CollectUiEvent>

    // 初始化订阅全局事件
    fun registerCollectEvent(coroutineScope: CoroutineScope)

    // 点赞/取消点赞
    fun toggleCollect(coroutineScope: CoroutineScope, article: Article)
}

// 💡 声明收藏相关的 UI 事件
sealed interface CollectUiEvent {
    data class ShowToast(val message: String) : CollectUiEvent // 显示提示
    object NavigateToLogin : CollectUiEvent                     // 跳转登录页
}