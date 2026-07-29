package com.example.wanandroiddemo.ui.coin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.wanandroiddemo.data.model.domain.CoinRecord
import com.example.wanandroiddemo.data.model.domain.UserCoin
import com.example.wanandroiddemo.data.repository.CoinRepository
import com.example.wanandroiddemo.ui.common.delegate.MessageDelegate
import com.example.wanandroiddemo.util.ext.coRunCatching
import com.example.wanandroiddemo.util.ext.logOnFailure
import com.example.wanandroiddemo.util.ext.mapNetworkException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoinViewModel @Inject constructor(
    private val repository: CoinRepository,
    private val messageDelegate: MessageDelegate
) : ViewModel(), MessageDelegate by messageDelegate {

    // 观察本地实时变化的积分总数
    val localCoin: StateFlow<UserCoin?> = repository.localCoinFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // 积分分页流
    val coinHistory: Flow<PagingData<CoinRecord>> = repository.getCoinHistoryStream()
        .cachedIn(viewModelScope)


    init {
        refreshCoinInfo()
    }

    /**
     * 主动向服务器请求最新积分数据
     */

    private fun refreshCoinInfo() {
        viewModelScope.launch {
            coRunCatching {
                repository.getUserCoin()
            }.logOnFailure("获取积分等级失败")
                .mapNetworkException()
                .onFailure {emitMessage(it.message?:"获取积分失败") }
        }
    }
}