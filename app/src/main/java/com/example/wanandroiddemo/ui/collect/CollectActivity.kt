package com.example.wanandroiddemo.ui.collect

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wanandroiddemo.R
import com.example.wanandroiddemo.base.BaseActivity
import com.example.wanandroiddemo.data.model.domain.toArticle
import com.example.wanandroiddemo.databinding.ActivityCollectBinding
import com.example.wanandroiddemo.ui.adapter.ArticleAdapter
import com.example.wanandroiddemo.ui.bookmark.BookmarkAdapter
import com.example.wanandroiddemo.ui.bookmark.BookmarkUiState
import com.example.wanandroiddemo.ui.bookmark.EditBookmarkDialogFragment
import com.example.wanandroiddemo.ui.web.ArticleDetailActivity
import com.example.wanandroiddemo.util.ext.collectMessages
import com.example.wanandroiddemo.util.ext.showConfirmDialog
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CollectActivity : BaseActivity<ActivityCollectBinding>() {
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var bookmarkAdapter: BookmarkAdapter
    private val viewModel: CollectViewModel by viewModels()

    override fun getViewBinding(inflater: LayoutInflater): ActivityCollectBinding {
        return ActivityCollectBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        // 文章适配器
        articleAdapter = ArticleAdapter { viewModel.toggleCollect(lifecycleScope, it) }
        binding.rvCollectList.setup(articleAdapter, this)
        articleAdapter.setOnItemClickListener { ArticleDetailActivity.start(this, it) }

        // 书签适配器
        bookmarkAdapter =
            BookmarkAdapter(
                onItemClick = { ArticleDetailActivity.start(this, it.toArticle()) },
                onEditClick = {
                    EditBookmarkDialogFragment.newInstance(it).show(supportFragmentManager, "Edit")
                },
                onDeleteClick = {
                    showConfirmDialog(
                        "提示",
                        "删除 [${it.name}]?",
                        onConfirm = { viewModel.deleteBookmark(it.id) })
                })
        binding.rvBookmarkList.apply {
            layoutManager = LinearLayoutManager(this@CollectActivity)
            adapter = bookmarkAdapter
            addItemDecoration(
                DividerItemDecoration(
                    this@CollectActivity, DividerItemDecoration.VERTICAL
                ).apply {
                    setDrawable(
                        ContextCompat.getDrawable(
                            this@CollectActivity, R.drawable.shape_list_divider
                        )!!
                    )
                })
        }

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val isArticle = tab?.position == 0
                binding.slArticles.isVisible = isArticle
                binding.slBookmarks.isVisible = !isArticle

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.swipeRefresh.setOnRefreshListener { viewModel.fetchBookmarks() }
    }

    override fun initData() {
        lifecycleScope.launch {
            viewModel.collectFlow.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { articleAdapter.submitData(it) }
        }

        lifecycleScope.launch {
            viewModel.bookmarkState.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { state ->
                    handleUiState(state)
                }
        }

        collectMessages(viewModel)

        lifecycleScope.launch {
            articleAdapter.loadStateFlow.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { handleUiState(it) }
        }
    }

    private fun handleUiState(state: BookmarkUiState) {
        binding.swipeRefresh.isRefreshing = state is BookmarkUiState.Loading
        when (state) {
            is BookmarkUiState.Success -> {
                bookmarkAdapter.submitList(state.list) {
                    if (bookmarkAdapter.itemCount == 0) {
                        binding.slBookmarks.showEmpty()
                    } else {
                        binding.slBookmarks.showContent()
                    }
                }
            }

            is BookmarkUiState.Error -> {
                binding.slBookmarks.showError(state.message) {
                    viewModel.fetchBookmarks()
                }
            }

            else -> {}
        }
    }

    private fun handleUiState(loadStates: CombinedLoadStates) {
        when (val refresh = loadStates.refresh) {
            is LoadState.Loading -> if (articleAdapter.itemCount == 0) binding.slArticles.showLoading()
            is LoadState.NotLoading -> {
                if (articleAdapter.itemCount == 0) binding.slArticles.showEmpty() else binding.slArticles.showContent()
            }
            is LoadState.Error -> binding.slArticles.showError(refresh.error.message ?: "失败") {
                articleAdapter.retry()
            }
        }
    }
}
