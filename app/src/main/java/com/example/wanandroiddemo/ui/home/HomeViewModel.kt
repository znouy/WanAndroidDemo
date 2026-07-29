package com.example.wanandroiddemo.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertHeaderItem
import androidx.paging.map
import com.example.wanandroiddemo.data.model.domain.Banner
import com.example.wanandroiddemo.data.model.domain.Article
import com.example.wanandroiddemo.data.repository.HomeRepository
import com.example.wanandroiddemo.data.repository.SettingsRepository
import com.example.wanandroiddemo.ui.common.delegate.CollectDelegate
import com.example.wanandroiddemo.ui.common.delegate.MessageDelegate
import com.example.wanandroiddemo.util.ext.coRunCatching
import com.example.wanandroiddemo.util.ext.logOnFailure
import com.example.wanandroiddemo.util.ext.mapNetworkException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 首页 ViewModel
 * 负责 Banner 数据获取及文章分页数据的管理
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository,
    private val settingsRepository: SettingsRepository,
    private val collectDelegate: CollectDelegate,
    private val messageDelegate: MessageDelegate
) : ViewModel(), CollectDelegate by collectDelegate, MessageDelegate by messageDelegate {

    // 是否显示置顶文章
    val showTopArticle: Flow<Boolean> = settingsRepository.settingsFlow.map { it.showTopArticle }
    val showBanner: Flow<Boolean> = settingsRepository.settingsFlow.map { it.showBanner }

    // 置顶文章缓存
    private val _topArticles = MutableStateFlow<List<Article>>(emptyList())
    val topArticles: StateFlow<List<Article>> = _topArticles

    // Banner 数据流状态
    private val _banners = MutableStateFlow<List<Banner>>(emptyList())
    val banners: StateFlow<List<Banner>> = _banners

    init {
        fetchBanners()
        fetchTopArticles()
        registerCollectEvent(viewModelScope)
    }

    /**
     * 构建置顶文章本地清洗流
     * 将置顶开关、置顶原始数据、本地收藏修改流（localModifications）三者结合
     * 这样只要用户在任何地方点了收藏，置顶文章的内存状态也会被自动清洗
     */
    private val cleanedTopArticles: Flow<List<Article>> = combine(
        showTopArticle,
        _topArticles,
        localModifications // 引入本地收藏状态修改流
    ) { show, list, modifications ->
        if (show) {
            list.map { article ->
                val localState = modifications[article.id]
                // 如果本地对该置顶文章有收藏操作，直接 copy 覆盖，否则使用默认状态
                if (localState != null) article.copy(collect = localState) else article
            }
        } else {
            emptyList()
        }
    }

    //文章列表流+top文章
    val articlesFlow: Flow<PagingData<Article>> =
        repository.getArticleFlow()
            .cachedIn(viewModelScope)
            // 让普通列表观察本地收藏流
            .combine(localModifications) { pagingData, modifications ->
                pagingData.map { article ->
                    val localState = modifications[article.id]
                    if (localState != null) article.copy(collect = localState) else article
                }
            }
            // 将已经过状态清洗的 [cleanedTopArticles] 追加进主列表
            .combine(cleanedTopArticles) { pagingData, topList ->
                if (topList.isEmpty()) {
                    pagingData
                } else {
                    // 使用 foldRight 将置顶文章列表依次追加到 PagingData 的头部
                    // 采用 foldRight（右折叠）可以完美保证 A -> B -> C 的原始排版顺序不被颠倒！
                    topList.foldRight(pagingData) { item, acc ->
                        acc.insertHeaderItem(item = item)
                    }
                }
            }

    /**
     * 获取 Banner 数据
     */
    private fun fetchBanners() {
        viewModelScope.launch {
            coRunCatching { repository.getBanners() }
                .logOnFailure("")
                .mapNetworkException()
                .onSuccess { _banners.value = it }
                .onFailure { emitMessage(it.message ?: "获取轮播图失败！") }
        }
    }

    private fun fetchTopArticles() {
        viewModelScope.launch {
            coRunCatching { repository.getTopArticles() }
                .logOnFailure("获取置顶文章失败")
                .mapNetworkException()
                .onSuccess { _topArticles.value = it }
                .onFailure { emitMessage(it.message ?: "获取置顶文章失败！") }
        }
    }

}
