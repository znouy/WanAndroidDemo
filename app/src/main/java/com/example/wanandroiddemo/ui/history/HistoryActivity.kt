package com.example.wanandroiddemo.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.example.wanandroiddemo.R
import com.example.wanandroiddemo.base.BaseActivity
import com.example.wanandroiddemo.data.model.domain.toArticle
import com.example.wanandroiddemo.databinding.ActivityHistoryBinding
import com.example.wanandroiddemo.ui.web.ArticleDetailActivity
import com.example.wanandroiddemo.util.ext.collectMessages
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class HistoryActivity : BaseActivity<ActivityHistoryBinding>() {

    lateinit var adapter: HistoryAdapter
    private val viewModel: HistoryViewModel by viewModels()
    override fun getViewBinding(inflater: LayoutInflater): ActivityHistoryBinding {
        return ActivityHistoryBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        adapter = HistoryAdapter(onItemClick = {
            ArticleDetailActivity.start(this, it.toArticle())
        }, onDeleteClick = {
            viewModel.deleteHistory(it.id)
        })
        binding.toolbar.setNavigationOnClickListener { finish() }
        // 点击右上角清空
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_clear_history) {
                showClearConfirmDialog()
            }
            true
        }

        //完美的组件化运用：一句话绑定本地 Paging 列表
        binding.historyList.setup(adapter, this)
    }

    override fun initData() {
        // 1. 观察本地 Room 发射的分页流
        lifecycleScope.launch {
            viewModel.historyFlow
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { pagingData ->
                    adapter.submitData(pagingData)
                }
        }

        // 2. 监听 Toast 提示事件
        collectMessages(viewModel)

        lifecycleScope.launch {
            adapter.loadStateFlow
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { states ->
                    handleUiState(states)
                }
        }
    }

    private fun handleUiState(states: CombinedLoadStates) {
        when (val refreshState = states.refresh) {
            is LoadState.Loading -> {
                if (adapter.itemCount == 0) {
                    binding.stateLayout.showLoading()
                }
            }

            is LoadState.Error -> {
                binding.stateLayout.showError(refreshState.error.message ?: "加载失败") {
                    adapter.retry()
                }
            }

            is LoadState.NotLoading -> {
                if (adapter.itemCount == 0) binding.stateLayout.showEmpty() else binding.stateLayout.showContent()
            }
        }
    }

    /**
     * 确认清空历史记录的弹窗
     */
    private fun showClearConfirmDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("提示")
            .setMessage("确定要清空所有的阅读历史记录吗？此操作不可撤销。")
            .setPositiveButton("确定") { dialog, _ ->
                viewModel.clearAll()
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}