package com.example.wanandroiddemo.ui.todo

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wanandroiddemo.R
import com.example.wanandroiddemo.base.BaseActivity
import com.example.wanandroiddemo.data.model.domain.Todo
import com.example.wanandroiddemo.databinding.ActivityTodoBinding
import com.example.wanandroiddemo.ui.adapter.ListFooterAdapter
import com.example.wanandroiddemo.ui.widget.loading.LoadingDelegate
import com.example.wanandroiddemo.ui.widget.loading.LoadingDelegateImpl
import com.example.wanandroiddemo.util.ext.collectLoading
import com.example.wanandroiddemo.util.ext.collectMessages
import com.example.wanandroiddemo.util.ext.onLoadMore
import com.example.wanandroiddemo.util.ext.showConfirmDialog
import com.example.wanandroiddemo.util.ext.showToast
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * TODO 列表展示页。
 *
 * 该页面实现了在非 Paging 3 场景下的传统分页加载模式，涵盖了下拉刷新与上拉加载的核心交互逻辑。
 *
 * ### 核心实现逻辑
 * 1. **分页状态管理**：自定义 `@PagingState` 进行分页状态管理，确保数据源同步。
 * 2. **列表布局**：使用 [ConcatAdapter] 合并数据列表 [TodoAdapter] 与脚布局 [ListFooterAdapter]。
 * 3. **脚布局管理**：
 *    - [ListFooterAdapter] 负责展示加载状态。
 *    - `FooterState` 枚举/密封类负责统一管理脚布局的多种显示状态。
 *
 * ### 上拉加载机制
 * - 引入扩展函数 [RecyclerView.onLoadMore] 监听列表滚动触底事件。
 *
 * ### 性能与安全性
 *   - 列表更新采用异步 DiffUtil。为避免 [IndexOutOfBoundsException] 异常，脚布局状态
 *     的更新必须在 [ListAdapter.submitList] 的 commit 回调中执行。
 *
 *
 * @see TodoViewModel
 * @see ListFooterAdapter
 */
@AndroidEntryPoint
class TodoActivity : BaseActivity<ActivityTodoBinding>(), LoadingDelegate {
    private lateinit var todoAdapter: TodoAdapter
    private lateinit var footerAdapter: ListFooterAdapter
    private val viewModel: TodoViewModel by viewModels()

    private val loadingDelegate by lazy { LoadingDelegateImpl(supportFragmentManager) }
    override fun showLoading(show: Boolean) {
        loadingDelegate.showLoading(show)
    }

    override fun getViewBinding(inflater: LayoutInflater): ActivityTodoBinding {
        return ActivityTodoBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        todoAdapter = TodoAdapter(
            onStatusChanged = { viewModel.toggleStatus(it) },
            onDeleteClick = { showDeleteConfirmDialog(it.id) },
            onEditClick = { showEditTodoDialog(it) })
        footerAdapter = ListFooterAdapter { viewModel.loadMore() }
        // 使用 ConcatAdapter 合并挂载
        val concatAdapter = ConcatAdapter(todoAdapter, footerAdapter)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@TodoActivity)
            adapter = concatAdapter
            onLoadMore(0) { viewModel.loadMore() }//触发上拉加载时调用
        }

        binding.toolbar.setOnClickListener { finish() }
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_add_bookmark) {
                showEditTodoDialog(null)
            }
            true
        }
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    Timber.d("tab:${tab.position}")
                    // 0: 待办，1: 已完成
                    viewModel.currentStatus.value = it.position
                    viewModel.isTabSwitch = true
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })

        binding.swipeRefresh.setOnRefreshListener { viewModel.refreshTodoList() }

    }

    override fun initData() {
        collectMessages(viewModel)//弹消息提醒
        collectLoading(viewModel.isActionLoading)//弹加载框

        lifecycleScope.launch {
            viewModel.uiState
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest {
                    handleUiState(it)
                }
        }

    }

    private fun handleUiState(state: TodoUiState) {
        when (state) {
            is TodoUiState.Loading -> {
                binding.swipeRefresh.isRefreshing = true
            }

            is TodoUiState.Success -> {
                binding.swipeRefresh.isRefreshing = false

                todoAdapter.submitList(groupTodoList(state.list)) {
                    // footerAdapter ui更新，必须在回调中调用，否则会角标越界异常
                    footerAdapter.currentState = state.footerState

                    if (viewModel.isTabSwitch) {
                        viewModel.isTabSwitch = false
                        binding.recyclerView.scrollToPosition(0)
                    }
                }
            }

            is TodoUiState.Error -> {
                viewModel.isTabSwitch = false
                binding.swipeRefresh.isRefreshing = false
                showToast(state.message)
            }
        }
    }

    private fun groupTodoList(list: List<Todo>): List<TodoAdapter.TodoItem> {
        val result = mutableListOf<TodoAdapter.TodoItem>()
        list.groupBy { it.dateStr }.forEach { (date, todos) ->
            result.add(TodoAdapter.TodoItem.DateHeader(date))
            todos.forEach { result.add(TodoAdapter.TodoItem.TodoData(it)) }
        }
        return result
    }

    /**
     * 精妙融合：一个对话框，同时闭环 “新增” 与 “编辑”
     */

    private fun showEditTodoDialog(todo: Todo?) {
        EditTodoDialogFragment.newInstance(todo).show(supportFragmentManager, "EditTodoDialog")
    }

    private fun showDeleteConfirmDialog(id: Int) {
        showConfirmDialog(
            "提示",
            "确定要删除这项日程规划吗？",
            onConfirm = { viewModel.deleteTodo(id) })

    }


}