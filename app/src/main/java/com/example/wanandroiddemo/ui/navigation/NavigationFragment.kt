package com.example.wanandroiddemo.ui.navigation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wanandroiddemo.base.BaseFragment
import com.example.wanandroiddemo.databinding.FragmentNavigationBinding
import com.example.wanandroiddemo.ui.web.ArticleDetailActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class NavigationFragment : BaseFragment<FragmentNavigationBinding>() {
    private lateinit var navAdapter: NavCategoryAdapter
    private lateinit var detailAdapter: NavDetailAdapter
    private val viewModel: NavigationViewModel by viewModels()

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentNavigationBinding.inflate(inflater, container, false)


    override fun initView() {
        // 点击联动右侧 RecyclerView 快速定位
        navAdapter = NavCategoryAdapter { position ->
            val layoutManager = binding.rvNavDetail.layoutManager as? LinearLayoutManager
            layoutManager?.scrollToPositionWithOffset(position, 0)
        }
        detailAdapter = NavDetailAdapter { article ->
            Timber.d("Item clicked: title=%s, link=%s", article.title, article.link)
            ArticleDetailActivity.start(requireContext(), article)
        }

        binding.rvNavList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = navAdapter
        }
        binding.rvNavDetail.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = detailAdapter

        }

        binding.srLayout.setOnRefreshListener { viewModel.reFetch() }
        // 右侧滚动时，自动高亮并联动左侧分类列表
        binding.rvNavDetail.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                val firstVisiblePosition = layoutManager?.findFirstVisibleItemPosition() ?: 0

                // 只有当位置确实改变时再更新，避免死循环或频繁刷新
                if (navAdapter.getSelectedPosition() != firstVisiblePosition) {
                    navAdapter.setSelectedPosition(firstVisiblePosition)
                    // 确保左侧对应的 Item 也滚动到视野中
                    binding.rvNavList.smoothScrollToPosition(firstVisiblePosition)
                }
            }
        })

    }

    override fun initData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { state ->
                    handleUiState(state)
                }
        }
    }

    private fun handleUiState(state: NavigationUiState) {
        when (state) {
            is NavigationUiState.Loading -> if (navAdapter.itemCount == 0) binding.stateLayout.showLoading()

            is NavigationUiState.Success -> {
                binding.srLayout.isRefreshing = false
                navAdapter.submitList(state.categories){
                    if (navAdapter.itemCount == 0) {
                        binding.stateLayout.showEmpty()
                    } else {
                        binding.stateLayout.showContent()
                    }
                }
                detailAdapter.submitList(state.categories)

            }

            is NavigationUiState.Error -> {
                binding.srLayout.isRefreshing = false
                binding.stateLayout.showError(state.message) {
                    viewModel.reFetch()
                }

            }
        }
    }


}
