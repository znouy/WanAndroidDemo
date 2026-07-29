package com.example.wanandroiddemo.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import com.example.wanandroiddemo.base.BaseFragment
import com.example.wanandroiddemo.databinding.FragmentHomeBinding
import com.example.wanandroiddemo.ui.adapter.ArticleAdapter
import com.example.wanandroiddemo.ui.home.BannerAdapter
import com.example.wanandroiddemo.ui.auth.LoginActivity
import com.example.wanandroiddemo.ui.common.delegate.CollectUiEvent
import com.example.wanandroiddemo.ui.web.ArticleDetailActivity
import com.example.wanandroiddemo.util.ext.collectMessages
import com.example.wanandroiddemo.util.ext.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 首页 Fragment
 * 使用 ConcatAdapter 拼接 Banner 和 文章列表
 */
@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: HomeViewModel by viewModels()

    // 私有可空变量，用于在 onDestroyView 中安全置空，杜绝内存泄漏
    private var _articleAdapter: ArticleAdapter? = null

    // ConcatAdapter
    private var _concatAdapter: ConcatAdapter? = null

    // 只读非空属性，供内部使用。使用时直接调用，不需要加 ?（如 articleAdapter.submitData）
    private val articleAdapter get() = _articleAdapter!!
    private val concatAdapter get() = _concatAdapter!!

    private val bannerAdapter = BannerAdapter()

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        _articleAdapter =
            ArticleAdapter { viewModel.toggleCollect(viewLifecycleOwner.lifecycleScope, it) }

        _concatAdapter = ConcatAdapter(bannerAdapter, articleAdapter)

        binding.pagingRecyclerView.setup(articleAdapter, viewLifecycleOwner)
        binding.pagingRecyclerView.getRecyclerView().adapter = concatAdapter

        // 监听加载状态

        articleAdapter.setOnItemClickListener { article ->
            ArticleDetailActivity.start(requireContext(), article)
        }
    }

    override fun initData() {
        //观察消息提示
        collectMessages(viewModel)

        //观察列表数据
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.showBanner.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { show ->
                    val hasBanner = bannerAdapter in concatAdapter.adapters
                    when {
                        show && !hasBanner -> concatAdapter.addAdapter(0, bannerAdapter)
                        !show && hasBanner -> concatAdapter.removeAdapter(bannerAdapter)
                    }
                }
        }
        //观察列表数据
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.banners.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { banners ->
                    bannerAdapter.submitList(banners)
                }
        }
        //观察列表数据
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.articlesFlow.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { pagingData ->
                    articleAdapter.submitData(pagingData)
                }
        }
        //观察点赞事件
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.collectUiEvent.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { event ->
                    handleCollectEvent(event)
                }
        }
        //观察界面状态变化
        viewLifecycleOwner.lifecycleScope.launch {
            articleAdapter.loadStateFlow.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { loadStates ->
                    handleUiState(loadStates)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _articleAdapter = null
        _concatAdapter = null
    }
}
