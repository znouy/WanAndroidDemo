package com.example.wanandroiddemo.ui.coin


import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.wanandroiddemo.R
import com.example.wanandroiddemo.base.BaseActivity
import com.example.wanandroiddemo.databinding.ActivityCoinBinding
import com.example.wanandroiddemo.util.ext.animateNumber
import com.example.wanandroiddemo.util.ext.collectMessages
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CoinActivity : BaseActivity<ActivityCoinBinding>() {
    private lateinit var historyAdapter: CoinHistoryAdapter
    private val viewModel: CoinViewModel by viewModels()

    override fun getViewBinding(inflater: LayoutInflater): ActivityCoinBinding {
        return ActivityCoinBinding.inflate(layoutInflater)
    }


    override fun initView(savedInstanceState: Bundle?) {
        binding.toolbar.title = getString(R.string.drawer_menu_integral)
        historyAdapter = CoinHistoryAdapter()
        // 利用系统的 DividerItemDecoration 挂载分割线
        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL).apply {
            // 将 drawable 样式设置给分割线
            setDrawable(
                ContextCompat.getDrawable(
                    this@CoinActivity,
                    R.drawable.shape_list_divider
                )!!
            )
        }
        binding.rvCoinList.setup(historyAdapter, this)
        binding.toolbar.setNavigationOnClickListener { finish() }

    }

    override fun initData() {
        //  监听最新积分总数，实时同步到页面头部展示
        lifecycleScope.launch {
            viewModel.localCoin
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { coin ->
                    // 头部大字显示总积分
                    binding.tvTotalCoin.animateNumber(coin?.coinCount?:0)

                }

        }

        // 2. 监听分页数据
        lifecycleScope.launch {
            viewModel.coinHistory
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { pagingData ->
                    historyAdapter.submitData(pagingData)
                }

        }

        // 3. 监听提示信息
        collectMessages(viewModel)

        lifecycleScope.launch {
            historyAdapter.loadStateFlow
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { loadStates ->
                    handleUiState(loadStates)
                }
        }

    }

    private fun handleUiState(loadStates: CombinedLoadStates) {
        when (val refreshState = loadStates.refresh) {
            is LoadState.Loading -> if (historyAdapter.itemCount == 0) binding.stateLayout.showLoading()
            is LoadState.NotLoading -> if (historyAdapter.itemCount == 0) {
                binding.stateLayout.showEmpty("暂无记录")
            } else {
                binding.stateLayout.showContent()
            }

            is LoadState.Error -> binding.stateLayout.showError(
                refreshState.error.message ?: "暂无记录"
            ) {
                historyAdapter.retry()
            }
        }
    }
}