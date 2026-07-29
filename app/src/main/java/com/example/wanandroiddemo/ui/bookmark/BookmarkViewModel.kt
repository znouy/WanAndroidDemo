package com.example.wanandroiddemo.ui.bookmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanandroiddemo.data.model.domain.Bookmark
import com.example.wanandroiddemo.data.repository.BookmarkRepository
import com.example.wanandroiddemo.ui.common.delegate.MessageDelegate
import com.example.wanandroiddemo.util.ext.coRunCatching
import com.example.wanandroiddemo.util.ext.isValidHttpUrl
import com.example.wanandroiddemo.util.ext.logOnFailure
import com.example.wanandroiddemo.util.ext.mapNetworkException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BookmarkUiState {
    object Loading : BookmarkUiState
    data class Success(val list: List<Bookmark>) : BookmarkUiState
    data class Error(val message: String) : BookmarkUiState
}

sealed interface BookMarkUiEvent {
    object DismissDialog : BookMarkUiEvent
}


@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val repository: BookmarkRepository,
    private val messageDelegate: MessageDelegate
) : ViewModel(), MessageDelegate by messageDelegate {

    private val _uiState = MutableStateFlow<BookmarkUiState>(BookmarkUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private val _uiEvent = Channel<BookMarkUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    // 动作类的 Loading，直接绑定给界面
    val isActionLoading = MutableStateFlow(false)

    init {
        fetchBookmarks()
    }

    /**
     * 场景一：拉取书签数据（读数据）
     * 采用 coRunCatching + logOnFailure 链式调用
     */
    fun fetchBookmarks() {
        viewModelScope.launch {
            _uiState.value = BookmarkUiState.Loading

            coRunCatching { repository.getBookmarks() }
                .logOnFailure()
                .mapNetworkException()
                .onSuccess { list ->
                    _uiState.value = BookmarkUiState.Success(list)
                }.onFailure { exception ->
                    _uiState.value = BookmarkUiState.Error(exception.message ?: "获取数据失败")
                }
        }
    }

    /**
     * 场景二：添加书签
     */
    fun addBookmark(name: String, link: String) {
        val error = validateBookmark(name, link)
        if (error != null) {
            emitMessage(error)
            return
        }
        viewModelScope.launch {
            // 2. 发起请求并重新刷新整个列表（添加操作有排序，重新加载更合逻辑）
            coRunCatching(isActionLoading) { repository.addBookmark(name, link) }
                .logOnFailure()
                .mapNetworkException()
                .onSuccess {
                    emitMessage("添加成功")
                    _uiEvent.send(BookMarkUiEvent.DismissDialog)
                    fetchBookmarks()
                }.onFailure {
                    emitMessage("添加失败")
                }
        }
    }

    private fun validateBookmark(name: String, link: String): String? {
        return when {
            name.isEmpty() -> "书签名称不能为空"
            link.isEmpty() -> "书签网址不能为空"
            !link.isValidHttpUrl() -> "书签网址请以 http:// 或 https:// 开头"
            else -> null
        }
    }

    /**
     * 场景三：修改书签
     */
    fun updateBookmark(id: Int, name: String, link: String) {
        val error = validateBookmark(name, link)
        if (error != null) {
            emitMessage(error)
            return
        }

        if (link.isValidHttpUrl())
            viewModelScope.launch {
                coRunCatching(isActionLoading) { repository.updateBookmark(id, name, link) }
                    .logOnFailure()
                    .mapNetworkException()
                    .onSuccess {
                        _uiEvent.send(BookMarkUiEvent.DismissDialog)
                        emitMessage("修改成功")
                        fetchBookmarks()
                    }
                    .onFailure { exception ->
                        emitMessage(exception.message ?: "修改失败")
                    }
            }
    }

    /**
     * 场景四：删除书签（本地内存级响应式删除）
     *  使用 safeLaunch 协程，在 finally 中自动关闭转圈。
     *  成功后，不用请求网络重新刷回第一页，直接原地用 filter 进行内存删除，显示丝滑删除动画！
     */
    fun deleteBookmark(id: Int) {
        viewModelScope.launch {
            coRunCatching(isActionLoading) { repository.deleteBookmark(id) }
                .logOnFailure()
                .mapNetworkException()
                .onSuccess {
                    val currentState = _uiState.value
                    if (currentState is BookmarkUiState.Success) {
                        val updatedList = currentState.list.filter { it.id != id }
                        _uiState.value = BookmarkUiState.Success(updatedList)
                    }
                    emitMessage("删除成功")
                }
                .onFailure { exception ->
                    emitMessage(exception.message ?: "删除失败")
                }
        }
    }
}