package com.example.wanandroiddemo.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.wanandroiddemo.data.model.domain.ReadHistory
import com.example.wanandroiddemo.data.repository.HistoryRepository
import com.example.wanandroiddemo.ui.common.delegate.MessageDelegate
import com.example.wanandroiddemo.util.ext.coRunCatching
import com.example.wanandroiddemo.util.ext.logOnFailure
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: HistoryRepository,
    private val messageDelegate: MessageDelegate
) : ViewModel(), MessageDelegate by messageDelegate {


    val historyFlow: Flow<PagingData<ReadHistory>> = repository.getHistoryStream()
        .cachedIn(viewModelScope)
    val isActionLoading = MutableStateFlow(false)

    init {
        Timber.d("⚙️ [HistoryVM] 实例已创建")
    }

    // 清空历史记录
    fun clearAll() {
        viewModelScope.launch {
            Timber.d("👉 [HistoryVM] 用户请求清空历史记录")
            coRunCatching(isActionLoading) {
                repository.clearAllHistory()
            }.logOnFailure("HistoryVM")
                .onSuccess {
                    Timber.d("✅ [HistoryVM] 历史记录已全部清空")
                    emitMessage("历史记录已清空")
                }.onFailure { emitMessage("清空失败，请重试") }
        }
    }

    fun deleteHistory(id : Int) {
        viewModelScope.launch {
            Timber.d("👉 [HistoryVM] 用户请求删除单个历史记录")
            coRunCatching(isActionLoading) {
                repository.deleteHistory(id)
            }.logOnFailure("deleteHistory failure")
                .onSuccess {
                    Timber.d("✅ [HistoryVM] 历史记录已全部清空")
                    emitMessage("删除成功")
                }.onFailure { emitMessage(it.message?:"删除失败，请重试") }
        }
    }

}