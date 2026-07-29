package com.example.wanandroiddemo.ui.share

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.example.wanandroiddemo.R
import com.example.wanandroiddemo.base.BaseActivity
import com.example.wanandroiddemo.databinding.ActivityShareBinding
import com.example.wanandroiddemo.ui.adapter.ArticleAdapter
import com.example.wanandroiddemo.ui.auth.LoginActivity
import com.example.wanandroiddemo.ui.common.delegate.CollectUiEvent
import com.example.wanandroiddemo.ui.web.ArticleDetailActivity
import com.example.wanandroiddemo.util.ext.collectMessages
import com.example.wanandroiddemo.util.ext.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShareActivity : BaseActivity<ActivityShareBinding>() {

    lateinit var addShareLauncher: ActivityResultLauncher<Intent?>
    private val viewModel: ShareViewModel by viewModels()

    // 适配器绑定点击卡片点赞，以及长按卡片删除自己分享的文章
    private val adapter by lazy {
        ArticleAdapter(
            onCollectClick = { article ->
                // 调用插件委托的 toggleCollect
                viewModel.toggleCollect(lifecycleScope, article)
            }
        ).apply {
            isSwipeEnabled = true
            onDeleteClick = {
                viewModel.deleteSharedArticle(it.id) {
                    refresh() // 刷新列表，UI 局部丝滑删除该项
                }
            }
        }
    }

    override fun getViewBinding(inflater: LayoutInflater): ActivityShareBinding {
        return ActivityShareBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        // 绑定分页加载核心组件
        binding.shareList.setup(adapter, this)

        addShareLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    // 静默重刷新主分页列表，防止无用刷新
                    adapter.refresh()
                }
            }

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.action_add_share) {
                addShareLauncher.launch(Intent(this, AddShareActivity::class.java))
            }
            true
        }
        adapter.setOnItemClickListener { ArticleDetailActivity.start(this, it) }

    }

    override fun initData() {
        collectMessages(viewModel)
        // 观察数据
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.shareArticlesFlow.collectLatest { pagingData ->
                    adapter.submitData(pagingData)
                }
            }
        }

        // 监听插件 UI 事件 (Toast & 跳转)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.collectUiEvent.collect { event ->
                    handleCollectEvent(event)
                }
            }
        }

        lifecycleScope.launch {
            adapter.loadStateFlow
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { loadStates ->
                    handleUiState(loadStates)
                }

        }

    }

    private fun handleCollectEvent(event: CollectUiEvent) {
        when (event) {
            is CollectUiEvent.ShowToast -> showToast(event.message)
            is CollectUiEvent.NavigateToLogin -> LoginActivity.start(this)
        }
    }

    private fun handleUiState(loadStates: CombinedLoadStates) {
        when (val refreshState = loadStates.refresh) {
            is LoadState.Loading -> if (adapter.itemCount == 0) binding.stateLayout.showLoading()
            is LoadState.NotLoading -> if (adapter.itemCount == 0) {
                binding.stateLayout.showEmpty("暂无记录")
            } else {
                binding.stateLayout.showContent()
            }

            is LoadState.Error -> binding.stateLayout.showError(
                refreshState.error.message ?: "暂无记录"
            ) {
                adapter.retry()
            }
        }
    }

}