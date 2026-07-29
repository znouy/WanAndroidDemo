package com.example.wanandroiddemo.ui.web

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.BundleCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.wanandroiddemo.base.BaseFragment
import com.example.wanandroiddemo.data.model.domain.Article
import com.example.wanandroiddemo.databinding.FragmentArticleDetailBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * 文章详情 Fragment
 */
@AndroidEntryPoint
class ArticleDetailFragment : BaseFragment<FragmentArticleDetailBinding>() {
    private var article: Article? = null
    private val viewModel: ArticleDetailViewModel by viewModels()

    companion object {
        private const val ARG_ARTICLE = "arg_article"

        /**
         * 💡 提供静态工厂方法创建 Fragment 实例
         */
        fun newInstance(article: Article): ArticleDetailFragment {
            return ArticleDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ARTICLE, article)
                }
            }
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentArticleDetailBinding {
        return FragmentArticleDetailBinding.inflate(inflater, container, false)
    }


    override fun initView() {
        // 1. 获取参数
        arguments?.let {
            article = BundleCompat.getParcelable(it, ARG_ARTICLE, Article::class.java)

        }


        // 3. 💡 让 Fragment 自己监听系统返回键，实现完美内聚
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.webView.canGoBack()) {
                        binding.webView.goBack() // 网页回退
                    } else {
                        // 禁用当前回调，并让 Activity 正常退出
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
        initWebviewListener()
    }

    private fun initWebviewListener() {
        binding.webView.onProgressChangedListener = { progress ->
            binding.progressBar.progress = progress
            binding.progressBar.isVisible = progress < 100
        }
        binding.webView.onPageFinishedListener = {
            //  静默调用：插入阅读历史
            article?.let { viewModel.recordHistory(it) }
        }
    }


    override fun initData() {
        article?.let {
            if (it.link.isNotEmpty()) {
                binding.webView.loadUrl(it.link)
            }
        }

    }

    override fun onDestroyView() {
        // 调用安全销毁方法，彻底防止内存泄漏
        binding.webView.destroySafely()
        super.onDestroyView()
    }

}
