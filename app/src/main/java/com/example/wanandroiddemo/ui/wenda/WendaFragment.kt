package com.example.wanandroiddemo.ui.wenda


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.example.wanandroiddemo.base.BaseFragment
import com.example.wanandroiddemo.databinding.FragmentWendaBinding
import com.example.wanandroiddemo.ui.adapter.ArticleAdapter //
import com.example.wanandroiddemo.ui.auth.LoginActivity
import com.example.wanandroiddemo.ui.common.delegate.CollectUiEvent
import com.example.wanandroiddemo.ui.web.ArticleDetailActivity
import com.example.wanandroiddemo.util.ext.collectMessages
import com.example.wanandroiddemo.util.ext.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WendaFragment : BaseFragment<FragmentWendaBinding>() {

    private val viewModel: WendaViewModel by viewModels()

    private val articleAdapter by lazy {
        ArticleAdapter(
            onCollectClick = { article ->
                viewModel.toggleCollect(viewLifecycleOwner.lifecycleScope, article)
            }
        ).apply {
            setOnItemClickListener { article ->
                ArticleDetailActivity.start(requireContext(), article)
            }
        }
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentWendaBinding.inflate(inflater, container, false)

    override fun initView() {
        // ：传入适配器和生命周期，一自动绑定下拉刷新、上拉加载
        binding.pagingList.setup(articleAdapter, viewLifecycleOwner)
    }

    override fun initData() {
        // 监听问答数据流
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.wendaFlow.collectLatest { pagingData ->
                    articleAdapter.submitData(pagingData)
                }
            }
        }

        // 监听全局 Toast 事件（Event）
        collectMessages(viewModel)

        //监听加载状态
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                articleAdapter.loadStateFlow.collectLatest {states ->
                    handleUiState(states)
                }
            }
        }
        //观察点赞事件
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.collectUiEvent.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { event ->
                    handleCollectEvent(event)
                }
        }
    }
    private fun handleCollectEvent(event: CollectUiEvent) {
        when (event) {
            is CollectUiEvent.ShowToast -> requireContext().showToast(event.message)
            is CollectUiEvent.NavigateToLogin -> LoginActivity.start(requireContext())
        }
    }
    private fun handleUiState(loadStates: CombinedLoadStates) {
        when (val refreshState = loadStates.refresh) {
            is LoadState.Loading -> {
                if (articleAdapter.itemCount == 0) {
                    binding.stateLayout.showLoading()
                }
            }

            is LoadState.Error -> {
                binding.stateLayout.showError(
                    refreshState.error.message ?: "加载失败"
                ) {
                    articleAdapter.retry()
                }
            }

            is LoadState.NotLoading -> {
                if (articleAdapter.itemCount == 0) binding.stateLayout.showEmpty() else binding.stateLayout.showContent()
            }
        }
    }
}