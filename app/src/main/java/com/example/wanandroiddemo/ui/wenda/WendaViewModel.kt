package com.example.wanandroiddemo.ui.wenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.wanandroiddemo.data.model.domain.Article
import com.example.wanandroiddemo.data.repository.WendaRepository
import com.example.wanandroiddemo.ui.common.delegate.CollectDelegate
import com.example.wanandroiddemo.ui.common.delegate.MessageDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class WendaViewModel @Inject constructor(
    private val repository: WendaRepository,
    private val collectDelegate: CollectDelegate, //  注入收藏契约
    private val messageDelegate: MessageDelegate  //  注入消息契约
) : ViewModel(), CollectDelegate by collectDelegate, MessageDelegate by messageDelegate {

    init {
        registerCollectEvent(viewModelScope)
    }

    //  问答分页数据流：combine 合并本地收藏瞬时修改状态
    val wendaFlow: Flow<PagingData<Article>> =
        repository.getWendaFlow().cachedIn(viewModelScope)
            .combine(localModifications) { pagingData, modifications ->
                pagingData.map { article ->
                    val localState = modifications[article.id]
                    if (localState != null) article.copy(collect = localState) else article
                }
            }
            .cachedIn(viewModelScope) // 在末端安全缓存
}