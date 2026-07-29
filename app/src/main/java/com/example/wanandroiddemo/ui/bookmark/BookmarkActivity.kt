package com.example.wanandroiddemo.ui.bookmark

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wanandroiddemo.R
import com.example.wanandroiddemo.base.BaseActivity
import com.example.wanandroiddemo.data.model.domain.Bookmark
import com.example.wanandroiddemo.data.model.domain.toArticle
import com.example.wanandroiddemo.databinding.ActivityBookmarkBinding
import com.example.wanandroiddemo.ui.web.ArticleDetailActivity
import com.example.wanandroiddemo.ui.widget.loading.LoadingDelegate
import com.example.wanandroiddemo.ui.widget.loading.LoadingDelegateImpl
import com.example.wanandroiddemo.util.ext.collectLoading
import com.example.wanandroiddemo.util.ext.collectMessages
import com.example.wanandroiddemo.util.ext.showConfirmDialog
import com.example.wanandroiddemo.util.ext.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookmarkActivity : BaseActivity<ActivityBookmarkBinding>(), LoadingDelegate {

    private lateinit var bookAdapter: BookmarkAdapter
    private val viewModel: BookmarkViewModel by viewModels()
    private val loadingDelegate by lazy { LoadingDelegateImpl(supportFragmentManager) }
    override fun showLoading(show: Boolean) {
        loadingDelegate.showLoading(show)
    }

    override fun getViewBinding(inflater: LayoutInflater): ActivityBookmarkBinding {
        return ActivityBookmarkBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        bookAdapter = BookmarkAdapter(
            onItemClick = { bookmark ->
                ArticleDetailActivity.start(this, bookmark.toArticle())
            },
            onEditClick = { bookmark ->
                showEditBookmarkDialog(bookmark) // 编辑弹窗
            },
            onDeleteClick = { bookmark ->
                showDeleteConfirmDialog(bookmark) // 删除二次确认弹窗
            }
        )
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_add_bookmark) {
                showEditBookmarkDialog(null)
                true
            } else {
                false
            }
        }
        binding.toolbar.inflateMenu(R.menu.bookmark_toolbar_menu)
        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL).apply {
            // 将 drawable 样式设置给分割线
            setDrawable(
                ContextCompat.getDrawable(
                    this@BookmarkActivity,
                    R.drawable.shape_list_divider
                )!!
            )
        }
        // 绑定 RecyclerView 列表
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@BookmarkActivity)
            adapter = bookAdapter
            addItemDecoration(divider)
        }

        // 下拉监听
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchBookmarks()
        }
    }

    override fun initData() {
        // 1. 任务一：独立观察渲染页面三态的数据流
        lifecycleScope.launch {
            viewModel.uiState
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { state ->
                    handleUiState(state)
                }

        }

        // 2. 任务二：独立观察一次性 UI 通道事件（Toast ）
        collectMessages(viewModel)

        // 3.任务三：独立观察操作类 Loading，弹窗转圈控制
        collectLoading(viewModel.isActionLoading)
    }

    private fun handleUiState(state: BookmarkUiState) {
        when (state) {
            is BookmarkUiState.Loading -> {
                binding.swipeRefresh.isRefreshing = true
            }

            is BookmarkUiState.Success -> {
                binding.swipeRefresh.isRefreshing = false
                bookAdapter.submitList(state.list)
            }

            is BookmarkUiState.Error -> {
                binding.swipeRefresh.isRefreshing = false
                showToast(state.message)
            }
        }
    }

    /**
     * 添加/修改书签的复用弹窗（结合 ViewBinding）
     */
    private fun showEditBookmarkDialog(bookmark: Bookmark?) {
        EditBookmarkDialogFragment.newInstance(bookmark)
            .show(supportFragmentManager, "EditBookmark")
    }

    /**
     * 删除确认弹窗
     */
    private fun showDeleteConfirmDialog(bookmark: Bookmark) {
        showConfirmDialog(
            "提示", "确定要删除书签 [${bookmark.name}] 吗？",
            onConfirm = { viewModel.deleteBookmark(bookmark.id) })

    }


}