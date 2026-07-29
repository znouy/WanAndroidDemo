package com.example.wanandroiddemo.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanandroiddemo.data.model.domain.Todo
import com.example.wanandroiddemo.data.repository.TodoRepository
import com.example.wanandroiddemo.ui.adapter.FooterState
import com.example.wanandroiddemo.ui.common.delegate.MessageDelegate
import com.example.wanandroiddemo.util.PagingState
import com.example.wanandroiddemo.util.ext.coRunCatching
import com.example.wanandroiddemo.util.ext.logOnFailure
import com.example.wanandroiddemo.util.ext.mapNetworkException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed interface TodoUiState {
    object Loading : TodoUiState
    data class Success(
        val list: List<Todo>,
        val footerState: FooterState
    ) : TodoUiState

    data class Error(val message: String) : TodoUiState
}

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val repository: TodoRepository,
    private val messageDelegate: MessageDelegate
) : ViewModel(), MessageDelegate by messageDelegate {
    var isTabSwitch: Boolean = false // 标记：是否是由于切换 Tab 导致的加载

    //界面状态
    private val _uiState = MutableStateFlow<TodoUiState>(TodoUiState.Loading)
    val uiState = _uiState.asStateFlow()

    //提示loading
    val isActionLoading = MutableStateFlow(false)

    //分页状态
    private var pagingState = PagingState<Todo>()

    //任务完成和未完成的状态 0: 未完成，1: 已完成
    val currentStatus = MutableStateFlow(0)

    init {
        Timber.d("⚙️ [TodoViewModel] 实例已创建并初始化")
        viewModelScope.launch {
            currentStatus.collect { refreshTodoList() }
        }
    }

    //下拉刷新：直接重新生成初始 PagingState（page 自动归位为 1）
    fun refreshTodoList() {
        pagingState = PagingState(isLoading = true)
        fetchTodoList(true)
    }

    //上拉加载：仅需要加锁拦截，绝对不在此时预增页码，确保状态与物理逻辑 100% 同步
    fun loadMore() {
        if (pagingState.isLoading || !pagingState.hasMore) return
        // 上拉加载中，立刻发送新状态，改变脚布局
        pagingState = pagingState.copy(isLoading = true)
        _uiState.value = TodoUiState.Success(pagingState.list, FooterState.Loading)
        fetchTodoList(false)
    }

    /**
     * 获取任务
     * */
    private fun fetchTodoList(isRefresh: Boolean) {
        viewModelScope.launch {
            Timber.d("👉 准备请求第 ${pagingState.page} 页数据, isRefresh: $isRefresh")
            if (isRefresh) {
                _uiState.value = TodoUiState.Loading
            }
            coRunCatching {
                repository.getTodoList(
                    pagingState.page, currentStatus.value
                )
            }.mapNetworkException()
                .logOnFailure("获取任务失败")
                .onSuccess { newPageList ->
                    //成功后更新下个分页状态
                    pagingState = pagingState.toNextSuccess(newPageList, isRefresh)
                    _uiState.value = TodoUiState.Success(
                        pagingState.list, footerState = when {
                            //将底层 PagingState 翻译为外部直接消费的复合状态
                            pagingState.list.isEmpty() -> FooterState.Idle
                            pagingState.isLoading -> FooterState.Loading
                            !pagingState.hasMore -> FooterState.NoMore
                            else -> FooterState.Idle
                        }
                    )
                }.onFailure { throwable ->
                    pagingState = pagingState.toFailure()
                    if (isRefresh) {
                        _uiState.value = TodoUiState.Error(throwable.message ?: "获取任务失败")
                    } else {
                        // 加载失败时，更新脚布局展示重试
                        _uiState.value = TodoUiState.Success(pagingState.list, FooterState.Error)
                    }
                }
        }
    }

    /**
     * 新增任务
     * */
    fun addTodo(title: String, content: String, date: String, priority: Int) {
        if (title.isEmpty()) {
            emitMessage("标题不能为空") // 通过消息代理，通知外部 Activity/Fragment 弹出 Toast
            return
        }
        viewModelScope.launch {
            coRunCatching(isActionLoading) {
                repository.addTodo(
                    title, content, date, priority
                )
            }.mapNetworkException()
                .logOnFailure()
                .onSuccess {
                    emitMessage("创建成功")
                    refreshTodoList()
                }
                .onFailure { emitMessage("创建失败") }
        }
    }

    /**
     *   删除任务
     * */
    fun deleteTodo(id: Int) {
        viewModelScope.launch {
            coRunCatching(isActionLoading) { repository.deleteTodo(id) }
                .mapNetworkException()
                .logOnFailure()
                .onSuccess {
                    val filterList = pagingState.list.filter { it.id != id }
                    pagingState = pagingState.copy(list = filterList)
                    _uiState.value = TodoUiState.Success(
                        pagingState.list,
                        footerState = if (pagingState.hasMore) FooterState.Idle else FooterState.NoMore
                    )
                    emitMessage("删除成功")
                }.onFailure { emitMessage("删除失败") }
        }
    }

    /**
     * 修改任务
     * */
    fun updateTodo(
        id: Int, title: String, content: String, date: String, isDone: Boolean, priority: Int
    ) {
        if (title.isEmpty()) {
            emitMessage("标题不能为空") // 通过消息代理，通知外部 Activity/Fragment 弹出 Toast
            return
        }
        viewModelScope.launch {
            coRunCatching(isActionLoading) {
                repository.updateTodo(
                    id, title, content, date, isDone, priority
                )
            }.mapNetworkException()
                .logOnFailure()
                .onSuccess {
                    emitMessage("修改成功")
                    refreshTodoList()
                }
                .onFailure { emitMessage("修改失败") }

        }
    }

    /**
     *  更改任务状态
     * */
    fun toggleStatus(todo: Todo) {
        viewModelScope.launch {
            val targetDone = !todo.isDone

            coRunCatching(isActionLoading) {
                repository.toggleStatus(todo.id, targetDone)
            }.mapNetworkException()
                .logOnFailure()
                .onSuccess {
                    val filterList = pagingState.list.filter { it.id != todo.id }
                    pagingState = pagingState.copy(list = filterList)
                    _uiState.value = TodoUiState.Success(
                        pagingState.list,
                        footerState = if (pagingState.hasMore) FooterState.Idle else FooterState.NoMore
                    )
                    emitMessage("操作成功")
                }
                .onFailure { emitMessage("操作失败") }

        }

    }

}