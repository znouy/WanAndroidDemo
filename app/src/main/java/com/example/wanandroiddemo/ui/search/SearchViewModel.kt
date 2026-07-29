package com.example.wanandroiddemo.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.wanandroiddemo.data.model.domain.Article
import com.example.wanandroiddemo.data.model.domain.HotKey
import com.example.wanandroiddemo.data.repository.SearchRepository
import com.example.wanandroiddemo.ui.common.delegate.CollectDelegate
import com.example.wanandroiddemo.ui.common.delegate.MessageDelegate
import com.example.wanandroiddemo.util.ext.coRunCatching
import com.example.wanandroiddemo.util.ext.logOnFailure
import com.example.wanandroiddemo.util.ext.mapNetworkException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

// ：只负责推荐与历史（因为搜索列表结果由 Paging 3 专属流独立承载）
sealed interface SuggestionUiState {
    object Loading : SuggestionUiState
    data class Success(val hotKeys: List<HotKey>, val history: List<String>) : SuggestionUiState
    data class Error(val message: String) : SuggestionUiState
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchRepository,
    private val   collectDelegate: CollectDelegate,
    private val messageDelegate: MessageDelegate
) : ViewModel(), CollectDelegate by collectDelegate, MessageDelegate by messageDelegate {

    private val _suggestionState = MutableStateFlow<SuggestionUiState>(SuggestionUiState.Loading)
    val suggestionState = _suggestionState.asStateFlow()

    private var hotKeysCache: List<HotKey> = emptyList()
    private var searchHistoryCache: List<String> = emptyList()

    // 状态控制阀：当前输入的搜索关键字
    private val _currentQuery = MutableStateFlow("")

    /**
     * 将关键字 Flow 管道通过 flatMapLatest 变形。
     * 只要 _currentQuery 被赋新值，底层会自动干掉旧的 Pager 分页流并开启全新的 Paging 管道，
     * 配合 cachedIn 实现完美的屏幕旋转数据无缝复原。
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResultFlow: Flow<PagingData<Article>> = _currentQuery
        .filter { it.isNotBlank() } // 过滤空输入
        .flatMapLatest { query ->
            repository.getSearchResult(query)
        }
        .cachedIn(viewModelScope) // 必须缓存于当前生命周期作用域

    init {
        Timber.d("⚙️ [SearchViewModel] 初始化，开始同步 DataStore")
        viewModelScope.launch {
            repository.searchHistoryFlow.collectLatest { history ->
                Timber.d("searchHistoryFlow:history=$history")
                searchHistoryCache = history
                if (_suggestionState.value is SuggestionUiState.Success) {
                    _suggestionState.value =
                        SuggestionUiState.Success(hotKeysCache, searchHistoryCache)
                }
            }
        }
        loadSuggestions()
    }

    /**
     * 获取搜索热词
     * */
    fun loadSuggestions() {
        viewModelScope.launch {
            _suggestionState.value = SuggestionUiState.Loading
            coRunCatching {
                if (hotKeysCache.isEmpty()) {
                    hotKeysCache = repository.getHotKeys()
                }
            }.logOnFailure("初始化失败")
                .mapNetworkException()
                .onSuccess {
                    _suggestionState.value =
                        SuggestionUiState.Success(hotKeysCache, searchHistoryCache)
                }
                .onFailure { exception ->
                    _suggestionState.value =
                        SuggestionUiState.Error(exception.message ?: "初始化失败")
                }
        }
    }

    fun submitSearch(keyword: String) {
        if (keyword.isBlank()) return
        viewModelScope.launch {
            repository.saveSearchHistory(keyword) // 保存历史到 DataStore
            _currentQuery.value = keyword        // 触发 flatMapLatest 管道喷涌新分页流
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearSearchHistory()
            emitMessage("历史记录已清空")
        }
    }
}