package com.example.wanandroiddemo.ui.square

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.wanandroiddemo.data.model.domain.Article
import com.example.wanandroiddemo.data.repository.SquareRepository
import com.example.wanandroiddemo.ui.common.delegate.CollectDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class SquareViewModel @Inject constructor(
    private val squareRepository: SquareRepository,
    private val collectDelegate: CollectDelegate, //  注入收藏插件
) : ViewModel(), CollectDelegate by collectDelegate {

    val squareArticleFlow: Flow<PagingData<Article>>

    init {
        // 3. 激活全局收藏事件监听（插件里的功能）
        registerCollectEvent(viewModelScope)

        // 4. 组合插件中的 localModifications 实现秒变色
        squareArticleFlow = squareRepository.getSquareArticles()
            .cachedIn(viewModelScope)
            .combine(localModifications) { pagingData, modifications ->
                pagingData.map { article ->
                    val localState = modifications[article.id]
                    if (localState != null) {
                        article.copy(collect = localState)
                    } else {
                        article
                    }
                }
            }
    }

}
