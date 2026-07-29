package com.example.wanandroiddemo.ui.square

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.example.wanandroiddemo.base.BaseFragment
import com.example.wanandroiddemo.databinding.FragmentSquareBinding
import com.example.wanandroiddemo.ui.adapter.ArticleAdapter
import com.example.wanandroiddemo.ui.auth.LoginActivity
import com.example.wanandroiddemo.ui.common.delegate.CollectUiEvent
import com.example.wanandroiddemo.ui.web.ArticleDetailActivity
import com.example.wanandroiddemo.util.ext.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class SquareFragment : BaseFragment<FragmentSquareBinding>() {

    private val viewModel: SquareViewModel by viewModels()
    private lateinit var articleAdapter: ArticleAdapter

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentSquareBinding.inflate(inflater, container, false)

    override fun initView() {
        articleAdapter = ArticleAdapter {
            viewModel.toggleCollect(viewLifecycleOwner.lifecycleScope, it)
        }
        binding.rvSquare.setup(articleAdapter, viewLifecycleOwner)
        articleAdapter.setOnItemClickListener { article ->
            Timber.d("Item clicked: title=%s, link=%s", article.title, article.link)
            ArticleDetailActivity.start(requireContext(), article)
        }
    }

    override fun initData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.squareArticleFlow
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collectLatest {
                    articleAdapter.submitData(it)
                }
        }

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
                .collect { loadStates ->
                    handleUiState(loadStates)
                }
        }
    }

    private fun handleUiState(loadStates: CombinedLoadStates) {
        when (val refreshState = loadStates.refresh) {
            is LoadState.Loading -> {
                if (articleAdapter.itemCount == 0) binding.stateLayout.showLoading()
            }

            is LoadState.Error -> {
                binding.stateLayout.showError(
                    refreshState.error.message ?: "加载失败"
                ) {
                    articleAdapter.retry()
                }
            }

            is LoadState.NotLoading -> {
                if (articleAdapter.itemCount == 0) {
                    binding.stateLayout.showEmpty()
                } else {
                    binding.stateLayout.showContent()
                }
            }
        }
    }

    private fun handleCollectEvent(event: CollectUiEvent) {
        when (event) {
            is CollectUiEvent.ShowToast -> requireContext().showToast(event.message)
            is CollectUiEvent.NavigateToLogin -> LoginActivity.start(requireContext())
        }
    }

    fun refresh() {
        articleAdapter.refresh()
    }
}
