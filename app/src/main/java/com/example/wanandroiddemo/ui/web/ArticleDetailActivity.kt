package com.example.wanandroiddemo.ui.web

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.content.IntentCompat
import com.example.wanandroiddemo.R
import com.example.wanandroiddemo.base.BaseActivity
import com.example.wanandroiddemo.data.model.domain.Article
import com.example.wanandroiddemo.databinding.ActivityArticleDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ArticleDetailActivity : BaseActivity<ActivityArticleDetailBinding>() {
    companion object {
        private const val EXTRA_ARTICLE = "extra_article"

        fun start(context: Context, article: Article) {
            Timber.d("Item clicked: article=%s, $article")
            val intent = Intent(context, ArticleDetailActivity::class.java).apply {
                putExtra(EXTRA_ARTICLE, article)
            }
            context.startActivity(intent)
        }
    }

    override fun getViewBinding(inflater: LayoutInflater): ActivityArticleDetailBinding {
        return ActivityArticleDetailBinding.inflate(inflater)
    }


    override fun initView(savedInstanceState: Bundle?) {
        // 获取 Article 对象
        val article =
            IntentCompat.getParcelableExtra(intent, EXTRA_ARTICLE, Article::class.java) ?: return

        //  检查 R.id.fragmentContainer 里有没有已存在的 Fragment
        val existingFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)

        // 如果 Fragment 还没被添加过，就通过 FragmentTransaction 将其装载进来
        if (existingFragment == null) {
            val webFragment = ArticleDetailFragment.newInstance(article)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, webFragment)
                .commit()
        }

        // 2. 初始化 Toolbar
        binding.toolbar.title = article.title
        binding.toolbar.setNavigationOnClickListener {
            //  触发系统级的返回事件，实现统一拦截
            onBackPressedDispatcher.onBackPressed()
        }
    }


}