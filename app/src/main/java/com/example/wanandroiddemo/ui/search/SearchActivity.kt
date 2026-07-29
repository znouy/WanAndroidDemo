package com.example.wanandroiddemo.ui.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import com.example.wanandroiddemo.base.BaseActivity
import com.example.wanandroiddemo.data.model.domain.HotKey
import com.example.wanandroiddemo.databinding.ActivitySearchBinding
import com.example.wanandroiddemo.databinding.ItemHotKeyBinding
import com.example.wanandroiddemo.ui.adapter.ArticleAdapter
import com.example.wanandroiddemo.ui.widget.SearchInputView
import com.example.wanandroiddemo.util.ext.collectMessages
import com.example.wanandroiddemo.util.ext.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchActivity : BaseActivity<ActivitySearchBinding>() {

    override fun getViewBinding(inflater: LayoutInflater): ActivitySearchBinding {
        return ActivitySearchBinding.inflate(inflater)
    }

    private val viewModel: SearchViewModel by viewModels()

    private val articleAdapter by lazy { ArticleAdapter {} }
    override fun initView(savedInstanceState: Bundle?) {
        // 1. 虚拟键控制：退回推荐页或关闭 Activity
        binding.toolbar.setNavigationOnClickListener {
            handleBackBehavior()
        }
        registerBackPressedDispatcher()//注册返回键双态阶梯回退监听
        // 绑定分页 RecyclerView 组件
        binding.pagingList.setup(articleAdapter, this)

        // 2. 官方统一接口：清空、改变、敲搜索键盘、输入，一个 Listener 全部完美拦截
        binding.searchView.setOnQueryTextListener(object : SearchInputView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val keyword = query?.trim().orEmpty()
                if (keyword.isNotEmpty()) {
                    hideKeyboard()
                    viewModel.submitSearch(keyword)
                } else {
                    showToast("请输入搜索词")
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    binding.stateLayout.isVisible = false
                    binding.layoutSuggestion.isVisible = true
                    viewModel.loadSuggestions() // 输入框为空（含点击 X 按钮），退回推荐界面
                }
                return true
            }
        })

        binding.tvClearHistory.setOnClickListener { viewModel.clearHistory() }
    }

    override fun initData() {
        // 收集Toast 提示
        collectMessages(viewModel)

        // 1. 订阅推荐栏 UI 状态
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.suggestionState.collectLatest { state ->
                    when (state) {
                        is SuggestionUiState.Loading -> {
                            binding.progressBar.isVisible = true
                        }

                        is SuggestionUiState.Success -> {
                            binding.progressBar.isVisible = false
                            binding.layoutSuggestion.isVisible = true
                            binding.stateLayout.isVisible = false // 隐藏列表大卡片

                            renderTags(state.hotKeys, state.history)
                        }

                        is SuggestionUiState.Error -> {
                            binding.progressBar.isVisible = false
                            Toast.makeText(this@SearchActivity, state.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }

        // 2. 订阅 Paging 3 数据流，由 StateLayout 独揽多态生命周期
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchResultFlow.collectLatest { pagingData ->
                    binding.layoutSuggestion.isVisible = false
                    binding.stateLayout.isVisible = true

                    articleAdapter.submitData(pagingData)
                }
            }
        }

        // 3.  数据流状态同步：利用 Paging 自身的 loadStateFlow 驱动 StateLayout 自动变态
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                articleAdapter.loadStateFlow.collectLatest { loadStates ->
                    when (val refreshState: LoadState = loadStates.refresh) {
                        is LoadState.NotLoading -> {
                            if (articleAdapter.itemCount == 0) {
                                // 展现精简后的 M3 单 TextView 占位图
                                binding.stateLayout.showEmpty("没有搜到相关文章，换个词试试吧")
                            } else {
                                // 展示真正的内容
                                binding.stateLayout.showContent()
                            }
                        }

                        is LoadState.Loading -> {
                            if (articleAdapter.itemCount == 0) binding.stateLayout.showLoading()
                        }

                        is LoadState.Error -> {
                            // 展现精简后的加载失败占位图，并绑定重试
                            binding.stateLayout.showError(
                                refreshState.error.message ?: "网络出了点小状况"
                            ) {
                                articleAdapter.retry()
                            }
                        }
                    }
                }
            }
        }


    }

    private fun handleBackBehavior() {
        if (binding.stateLayout.isVisible) {
            // 同步控制视图显隐：瞬间隐藏列表，展示推荐热词页
            binding.stateLayout.isVisible = false
            binding.layoutSuggestion.isVisible = true
            // 让输入框失去焦点，并强制关闭软键盘，否则返回是先处理这两个
            binding.searchView.clearFocus()

            // 重载热词历史（用于同步最新的 DataStore 历史）
            viewModel.loadSuggestions()
        } else {
            // 已经是推荐页面，直接安全退出 Activity
            finish()
        }
    }

    /**
     *  注册官方物理返回拦截监听
     */
    private fun registerBackPressedDispatcher() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackBehavior() // 执行统一拦截逻辑
            }
        })

//        binding.searchView.setOnBackPressListener { handleBackBehavior() }
    }

    private fun renderTags(hotKeys: List<HotKey>, history: List<String>) {
        binding.flowHotKeys.removeAllViews()
        val inflater = LayoutInflater.from(this)

        hotKeys.forEach { key ->
            val tagBinding = ItemHotKeyBinding.inflate(inflater, binding.flowHotKeys, false)
            tagBinding.root.text = key.name
            tagBinding.root.setOnClickListener {
                // 官方 API：设置输入框文字并自动拉取
                binding.searchView.setQuery(key.name)
            }
            binding.flowHotKeys.addView(tagBinding.root)
        }

        binding.flowHistory.removeAllViews()
        binding.layoutHistoryTitle.isVisible = history.isNotEmpty()

        history.forEach { item ->
            val tagBinding = ItemHotKeyBinding.inflate(inflater, binding.flowHistory, false)
            tagBinding.root.text = item
            tagBinding.root.setOnClickListener {
                binding.searchView.setQuery(item)
            }
            binding.flowHistory.addView(tagBinding.root)
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
    }
}