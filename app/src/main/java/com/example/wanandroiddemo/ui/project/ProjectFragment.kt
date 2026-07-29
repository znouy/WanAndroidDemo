package com.example.wanandroiddemo.ui.project

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.wanandroiddemo.base.BaseFragment
import com.example.wanandroiddemo.databinding.FragmentProjectBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProjectFragment : BaseFragment<FragmentProjectBinding>() {

    private val viewModel: ProjectViewModel by viewModels()

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentProjectBinding.inflate(inflater, container, false)

    override fun initView() {
    }

    override fun initData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collectLatest { state ->
                    handleUiState(state)
                }
        }
    }

    private fun handleUiState(state: ProjectUiState) {
        when (state) {
            is ProjectUiState.Loading -> binding.stateLayout.showLoading()

            is ProjectUiState.Success -> {
                val categories = state.data
                if (categories.isEmpty()) {
                    binding.stateLayout.showEmpty()
                } else {
                    binding.stateLayout.showContent()
                    if (binding.viewPager.adapter == null){//防止进入搜索页再返回时重新创建ProjectPagerAdapter
                        // 导致SystemArticleListFragment被重建，导致不必要的重新请求数据
                        binding.viewPager.adapter =
                            ProjectPagerAdapter(this@ProjectFragment, categories)
                        TabLayoutMediator(
                            binding.tabLayout,
                            binding.viewPager
                        ) { tab, position ->
                            tab.text = categories[position].name
                        }.attach()
                    }else{
                        // 如果数据有变化，更新 Adapter
                        // adapter.updateCategories(categories)

                        // 如果分类数量或顺序变了，TabLayoutMediator 可能需要重新 attach
                        // binding.tabLayout.removeAllTabs()
                        // TabLayoutMediator(...).attach()
                    }
                }
            }

            is ProjectUiState.Error -> binding.stateLayout.showError(state.message) {
                viewModel.reFetch()
            }
        }
    }
}
