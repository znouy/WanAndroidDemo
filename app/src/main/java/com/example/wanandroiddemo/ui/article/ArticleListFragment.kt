package com.example.wanandroiddemo.ui.article

// 1. ViewModel 用于处理分页数据
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.example.wanandroiddemo.base.BaseFragment
import com.example.wanandroiddemo.databinding.FragmentArticleListBinding
import com.example.wanandroiddemo.ui.adapter.ArticleAdapter
import com.example.wanandroiddemo.ui.auth.LoginActivity
import com.example.wanandroiddemo.ui.common.delegate.CollectUiEvent
import com.example.wanandroiddemo.ui.web.ArticleDetailActivity
import com.example.wanandroiddemo.util.ext.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


// 2. 对应的 Fragment
@AndroidEntryPoint
class ArticleListFragment : BaseFragment<FragmentArticleListBinding>() {

    private val viewModel: ArticleListViewModel by viewModels()
    private lateinit var articleAdapter: ArticleAdapter
    private var cid: Int = 0

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentArticleListBinding.inflate(inflater, container, false)

    override fun initView() {
        cid = arguments?.getInt("cid") ?: 0
        // 传入当前的分类 ID（可以是从 Bundle 传过来的）
        viewModel.setCategoryId(cid)

        articleAdapter =
            ArticleAdapter { viewModel.toggleCollect(viewLifecycleOwner.lifecycleScope, it) }

        binding.rvSystem.setup(articleAdapter, viewLifecycleOwner)


        articleAdapter.setOnItemClickListener { article ->
            ArticleDetailActivity.start(requireContext(), article)
        }
    }

    override fun initData() {
        //  收集文章分页数据
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.articlesFlow
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collectLatest { articleAdapter.submitData(it) }
        }
        //观察收藏事件
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.collectUiEvent
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { event ->
                    handleCollectEvent(event)
                }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            articleAdapter.loadStateFlow
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collectLatest { loadState ->
                    handleUiState(loadState)
                }
        }
    }

    private fun handleCollectEvent(event: CollectUiEvent) {
        when (event) {
            is CollectUiEvent.ShowToast -> requireContext().showToast(event.message)
            is CollectUiEvent.NavigateToLogin -> LoginActivity.start(requireContext())
        }
    }

    private fun handleUiState(loadState: CombinedLoadStates) {
        when (val refreshState = loadState.refresh) {
            is LoadState.Loading -> if (articleAdapter.itemCount == 0) binding.stateLayout.showLoading()
            is LoadState.Error -> binding.stateLayout.showError(
                refreshState.error.message ?: "加载失败"
            ) {
                articleAdapter.retry()
            }

            is LoadState.NotLoading ->
                if (articleAdapter.itemCount == 0) {
                    binding.stateLayout.showEmpty()
                } else {
                    binding.stateLayout.showContent()
                }
        }
    }


}
