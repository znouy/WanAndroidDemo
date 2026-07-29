package com.example.wanandroiddemo.ui.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.wanandroiddemo.data.model.domain.Article
import com.example.wanandroiddemo.data.repository.ShareRepository
import com.example.wanandroiddemo.ui.common.delegate.CollectDelegate
import com.example.wanandroiddemo.ui.common.delegate.MessageDelegate
import com.example.wanandroiddemo.util.ext.coRunCatching
import com.example.wanandroiddemo.util.ext.logOnFailure
import com.example.wanandroiddemo.util.ext.mapNetworkException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val repository: ShareRepository,
    collectDelegate: CollectDelegate, // 注入点赞插件
    messageDelegate: MessageDelegate
) : ViewModel(), CollectDelegate by collectDelegate,
    MessageDelegate by messageDelegate { // 类委托，赋予点赞功能

    val shareArticlesFlow: Flow<PagingData<Article>>
    val isActionLoading = MutableStateFlow(false)

    init {
        Timber.d("⚙️ [ShareVM] 实例已创建")

        // 激活插件全局收藏监听
        registerCollectEvent(viewModelScope)

        // 组合本地点赞修改，实现红心秒变色
        shareArticlesFlow = repository.getPrivateShareStream()
            .cachedIn(viewModelScope) // 双层缓存第一层：先缓存原始流防止 collect twice
            .combine(localModifications) { pagingData, modifications ->
                pagingData.map { article ->
                    val localState = modifications[article.id]
                    if (localState != null) article.copy(collect = localState) else article
                }
            }.cachedIn(viewModelScope) //  缓存最终成品
    }

    // 删除我分享的文章
    fun deleteSharedArticle(id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            Timber.d("👉 [ShareVM] 用户请求删除分享, ID: $id")

            coRunCatching(isActionLoading) { repository.deleteShare(id) }
                .logOnFailure("删除文章成功")
                .mapNetworkException()
                .onSuccess {
                    emitMessage("删除文章成功")
                    onSuccess()
                }.onFailure { emitMessage("删除文章失败") }
        }
    }
}