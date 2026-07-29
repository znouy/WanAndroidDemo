package com.example.wanandroiddemo.ui.system

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wanandroiddemo.base.BaseFragment
import com.example.wanandroiddemo.databinding.FragmentSystemBinding
import com.example.wanandroiddemo.ui.system.SystemAdapter
import com.example.wanandroiddemo.ui.article.ArticleListActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class SystemFragment : BaseFragment<FragmentSystemBinding>() {

    private val viewModel: SystemViewModel by viewModels()
    private lateinit var systemAdapter: SystemAdapter

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentSystemBinding.inflate(inflater, container, false)


    override fun initView() {
        systemAdapter = SystemAdapter().apply {
            setOnItemClickListener { category ->
                ArticleListActivity.start(requireContext(), category.id, category.name)
            }
        }
        binding.rvSystem.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = systemAdapter
        }
        binding.srLayout.setOnRefreshListener { viewModel.retryFetch() }
    }

    override fun initData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState
                .flowWithLifecycle(
                    viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED
                )
                .collectLatest { state -> handleUiState(state) }

        }
    }

    private fun handleUiState(state: SystemUiState) {
        when (state) {
            is SystemUiState.Loading -> {
                if (systemAdapter.itemCount == 0) binding.stateLayout.showLoading()
            }

            is SystemUiState.Success -> {
                binding.srLayout.isRefreshing = false
                systemAdapter.submitList(state.categories){//在 submitList 的异步计算【完毕回调】中再检查 itemCount
                    if (systemAdapter.itemCount == 0) {
                        binding.stateLayout.showEmpty()
                    } else {
                        binding.stateLayout.showContent()
                    }
                }

            }

            is SystemUiState.Error -> {
                binding.srLayout.isRefreshing = false
                binding.stateLayout.showError(state.message) {
                    viewModel.retryFetch()
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        Timber.d(" --- onDestroy-----------")
    }

}