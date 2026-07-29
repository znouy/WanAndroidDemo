package com.example.wanandroiddemo.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wanandroiddemo.databinding.ItemPagingFooterBinding
import com.example.wanandroiddemo.databinding.ViewPagingRecyclerBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * [PagingRecyclerView]
 *
 * 这是一个自包含、高度内聚的分页列表自定义控件（基于 Paging 3 规范）。
 * 内部深度集成了：
 * 1. 下拉刷新 (SwipeRefreshLayout) 状态全自动同步。
 * 2. 上拉自动加载更多 (无需手写任何滑动监听)。
 * 3. 底部多态脚布局 (加载中转圈 / 失败重试 / “我是有底线的”全部加载完毕提示)。
 * 4. 全自动适配 LinearLayoutManager 与 GridLayoutManager (支持 Footer 自动横跨整行)。
 *
 * -------------------------------------------------------------------------
 * 📝 【保姆级调用教程】
 *
 * 1. 在 XML 布局中声明：
 *    <com.example.wanandroiddemo.widget.PagingRecyclerView
 *        android:id="@+id/pagingList"
 *        android:layout_width="match_parent"
 *        android:layout_height="match_parent" />
 *
 * 2. 在 Fragment / Activity 中初始化绑定（仅需一行代码）：
 *    binding.pagingList.setup(myPagingDataAdapter, viewLifecycleOwner)
 *
 * 3. 观察 ViewModel 中的 PagingData 数据流并提交：
 *    lifecycleScope.launch {
 *        viewModel.articleFlow.collectLatest { pagingData ->
 *            myPagingDataAdapter.submitData(pagingData)
 *        }
 *    }
 * -------------------------------------------------------------------------
 *
 */
class PagingRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewPagingRecyclerBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    init {
        // 默认配置直立线性布局
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }

    /**
     * 一键配置下拉圈圈的主题颜色
     */
    fun setColorSchemeColors(vararg colors: Int) {
        binding.swipeRefresh.setColorSchemeColors(*colors)
    }

    /**
     *  核心外露接口：一键绑定 PagingDataAdapter 与 LifecycleOwner
     * 内部全自动接管：双向状态、手势刷新、Loading 同步、上拉 Footer 展示、网格跨行适配。
     */
    fun <T : Any, VH : RecyclerView.ViewHolder> setup(
        adapter: PagingDataAdapter<T, VH>,
        lifecycleOwner: LifecycleOwner
    ) {
        // 1. 实例化内部封装的专属脚适配器
        val footerAdapter = PagingLoadStateAdapter { adapter.retry() }

        // 2. 拼接挂载
        val concatAdapter = adapter.withLoadStateFooter(footer = footerAdapter)
        binding.recyclerView.adapter = concatAdapter

        // 3. 核心适配：全自动感应并兼容网格布局 (GridLayoutManager)
        val layoutManager = binding.recyclerView.layoutManager
        if (layoutManager is GridLayoutManager) {
            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    // 在 ConcatAdapter 中，最后一个 Item 必定是我们的上拉脚布局
                    val isFooter = position == concatAdapter.itemCount - 1
                    return if (isFooter) {
                        layoutManager.spanCount // 让脚布局横跨整行（占满所有的列）
                    } else {
                        1 // 正常数据项占 1 列
                    }
                }
            }
        }

        // 4. 自动绑定下拉刷新手势
        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh()
        }

        // 5. 自动监听加载状态，同步控制下拉刷新转圈的隐藏与展示
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { loadStates ->
                    binding.swipeRefresh.isRefreshing = loadStates.refresh is LoadState.Loading
                }
            }
        }
    }

    /**
     * 获取底层的 RecyclerView 引用（方便外部调用 addItemDecoration、smoothScroll 等方法）
     */
    fun getRecyclerView(): RecyclerView = binding.recyclerView

    // =====================================================================
    // 💡 内部封装高内聚组件（不对外暴露，保护逻辑安全，保证即插即用）
    // =====================================================================

    private class PagingLoadStateAdapter(
        private val retry: () -> Unit
    ) : LoadStateAdapter<PagingLoadStateAdapter.ViewHolder>() {

        /**
         * 重写此方法，让 Paging 3 在“加载完毕（NotLoading）”时，依然显示脚布局。
         * 这样我们新增加的 “— 我是有底线的 —” 状态才能被成功渲染出来。
         */
        override fun displayLoadStateAsItem(loadState: LoadState): Boolean {
            return loadState is LoadState.Loading ||
                    loadState is LoadState.Error ||
                    (loadState is LoadState.NotLoading && loadState.endOfPaginationReached)
        }

        override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ViewHolder {
            val binding = ItemPagingFooterBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding, retry)
        }

        override fun onBindViewHolder(holder: ViewHolder, loadState: LoadState) {
            holder.bind(loadState)
        }

        class ViewHolder(
            private val binding: ItemPagingFooterBinding,
            private val retry: () -> Unit
        ) : RecyclerView.ViewHolder(binding.root) {

            init {
                binding.btnRetry.setOnClickListener { retry() }
            }

            fun bind(loadState: LoadState) {
                // 1. Loading 状态下展示转圈进度条
                binding.progressBar.isVisible = loadState is LoadState.Loading

                // 2. Error 状态下展示扁平化重试文本
                binding.btnRetry.isVisible = loadState is LoadState.Error

                // 3. 核心判断：处于非加载状态，且已经触底（没有更多数据了），展示“有底线”提示
                binding.tvNoMore.isVisible = loadState is LoadState.NotLoading && loadState.endOfPaginationReached
            }
        }
    }
}