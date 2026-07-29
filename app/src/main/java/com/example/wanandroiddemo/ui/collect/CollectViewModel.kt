package com.example.wanandroiddemo.ui.collect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.example.wanandroiddemo.data.model.domain.Article
import com.example.wanandroiddemo.data.repository.BookmarkRepository
import com.example.wanandroiddemo.data.repository.CollectRepository
import com.example.wanandroiddemo.ui.bookmark.BookmarkUiState
import com.example.wanandroiddemo.ui.common.delegate.CollectDelegate
import com.example.wanandroiddemo.ui.common.delegate.MessageDelegate
import com.example.wanandroiddemo.util.ext.coRunCatching
import com.example.wanandroiddemo.util.ext.logOnFailure
import com.example.wanandroiddemo.util.ext.mapNetworkException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectViewModel @Inject constructor(
    private val repository: CollectRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val collectDelegate: CollectDelegate,
    private val messageDelegate: MessageDelegate
) : ViewModel(), CollectDelegate by collectDelegate, MessageDelegate by messageDelegate {

    // 收藏的文章列表数据流
    val collectFlow: Flow<PagingData<Article>> =
        repository.getCollectFollow()
            .cachedIn(viewModelScope)
            .combine(localModifications) { pagingData, modifications ->
                pagingData.filter { article ->
                    modifications[article.id] != false
                }
            }.cachedIn(viewModelScope)

    private val _bookmarkState = MutableStateFlow<BookmarkUiState>(BookmarkUiState.Loading)
    val bookmarkState = _bookmarkState.asStateFlow()

    init {
        registerCollectEvent(viewModelScope)
        fetchBookmarks()
    }

    fun fetchBookmarks() {
        viewModelScope.launch {
            _bookmarkState.value = BookmarkUiState.Loading
            coRunCatching { bookmarkRepository.getBookmarks() }
                .logOnFailure("getBookmarks failure")
                .mapNetworkException()
                .onSuccess { _bookmarkState.value = BookmarkUiState.Success(it) }
                .onFailure { _bookmarkState.value = BookmarkUiState.Error(it.message ?: "加载失败") }
        }
    }

    fun addBookmark(name: String, link: String) {
        viewModelScope.launch {
            coRunCatching { bookmarkRepository.addBookmark(name, link) }
                .onSuccess {
                    emitMessage("添加成功")
                    fetchBookmarks()
                }
                .onFailure { emitMessage("添加失败") }
        }
    }

    fun updateBookmark(id: Int, name: String, link: String) {
        viewModelScope.launch {
            coRunCatching { bookmarkRepository.updateBookmark(id, name, link) }
                .onSuccess {
                    emitMessage("修改成功")
                    fetchBookmarks()
                }
                .onFailure { emitMessage("修改失败") }
        }
    }

    fun deleteBookmark(id: Int) {
        viewModelScope.launch {
            coRunCatching { bookmarkRepository.deleteBookmark(id) }
                .logOnFailure("deleteBookmark failure")
                .mapNetworkException()
                .onSuccess {
                    val currentState = _bookmarkState.value
                    if (currentState is BookmarkUiState.Success) {
                        _bookmarkState.value = BookmarkUiState.Success(currentState.list.filter { it.id != id })
                    }
                    emitMessage("删除成功")
                }
                .onFailure { emitMessage("删除失败") }
        }
    }
}
