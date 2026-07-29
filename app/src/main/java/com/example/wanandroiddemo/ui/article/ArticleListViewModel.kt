package com.example.wanandroiddemo.ui.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.wanandroiddemo.data.model.domain.Article
import com.example.wanandroiddemo.data.repository.SystemRepository
import com.example.wanandroiddemo.ui.common.delegate.CollectDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ArticleListViewModel @Inject constructor(
    private val systemRepository: SystemRepository,
    collectDelegate: CollectDelegate
) : ViewModel(), CollectDelegate by collectDelegate {
    init {
        registerCollectEvent(viewModelScope)
    }

    // 1.声明一个代表分类 ID 的热流（初始值为 -1）
    private val _cidFlow = MutableStateFlow(-1)
    val articlesFlow: Flow<PagingData<Article>> = _cidFlow
        .filter { it != -1 }
        .flatMapLatest { cid ->
            // 每次 _cidFlow 的值发生变化，这里会自动切到新的分类数据流
            systemRepository.getSystemArticles(cid).cachedIn(viewModelScope)
        }
        .combine(localModifications) { pagingData, modifications ->
            pagingData.map { article ->
                val localState = modifications[article.id]
                if (localState != null)
                    article.copy(collect = localState)
                else
                    article
            }
        }
        .cachedIn(viewModelScope)

    // 3. 暴露给 Fragment 调用的方法，仅仅是修改 cid 的数值
    fun setCategoryId(cid: Int) {
        if (_cidFlow.value == cid) return // 防抖，防止重复请求
        _cidFlow.value = cid
    }

}