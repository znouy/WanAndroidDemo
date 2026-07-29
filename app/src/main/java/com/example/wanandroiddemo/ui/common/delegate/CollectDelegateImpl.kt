package com.example.wanandroiddemo.ui.common.delegate

import com.example.wanandroiddemo.data.local.AppPreferences
import com.example.wanandroiddemo.data.model.domain.Article
import com.example.wanandroiddemo.data.repository.CollectRepository
import com.example.wanandroiddemo.util.ext.coRunCatching
import com.example.wanandroiddemo.util.ext.logOnFailure
import com.example.wanandroiddemo.util.ext.mapNetworkException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 收藏功能插件的具体实现
 */
@Singleton
class CollectDelegateImpl @Inject constructor(
    private val collectRepository: CollectRepository,
    private val appPreferences: AppPreferences
) : CollectDelegate {

    private val _localModifications = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    override val localModifications: StateFlow<Map<Int, Boolean>> =
        _localModifications.asStateFlow()

    // 声明底层 Channel，设置 BUFFERED 缓冲区，确保后台事件不丢失
    private val _collectUiEvent = Channel<CollectUiEvent>(Channel.BUFFERED)

    // 转换为只读 Flow 暴露给外部
    override val collectUiEvent: Flow<CollectUiEvent> = _collectUiEvent.receiveAsFlow()

    /**
     * 向 localModifications 中写入一条修改记录
     * 这会立刻触发对应ViewMode的 combine 链条，自动向 UI 发射更新后的 PagingData
     * */
    override fun registerCollectEvent(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            // 订阅全局事件源，同步本地红心状态
            collectRepository.collectEvent.collect { (articleId, isCollected) ->
                _localModifications.update { current ->
                    current + (articleId to isCollected)
                }
            }
        }
    }

    override fun toggleCollect(coroutineScope: CoroutineScope, article: Article) {
        coroutineScope.launch {

            // 读取现有的 UserSession 状态，并判断是否登录
            val userSession = appPreferences.userSessionFlow.first()
            if (!userSession.isLogin) {
                // 未登录，直接向 UI 发送跳转登录事件
                _collectUiEvent.send(CollectUiEvent.NavigateToLogin)
                return@launch
            }

            // 读取本地暂存状态，没有则读取文章自身状态
            val isCurrentlyCollected = _localModifications.value[article.id] ?: article.collect
            val result = coRunCatching {
                if (isCurrentlyCollected) {
                    collectRepository.unCollectArticle(article.id)
                    "已取消收藏"
                } else {
                    collectRepository.collectArticle(article.id)
                    "收藏成功"
                }
            }.logOnFailure("收藏/取消收藏操作失败")
                .mapNetworkException()

            result.onSuccess { _collectUiEvent.send(CollectUiEvent.ShowToast(it)) }
                .onFailure {
                    _collectUiEvent.send(
                        CollectUiEvent.ShowToast(
                            it.message ?: "操作失败"
                        )
                    )
                }

        }
    }

}