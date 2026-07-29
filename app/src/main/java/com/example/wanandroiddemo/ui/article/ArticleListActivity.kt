package com.example.wanandroiddemo.ui.article

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import com.example.wanandroiddemo.R
import com.example.wanandroiddemo.base.BaseActivity
import com.example.wanandroiddemo.databinding.ActivityArticleListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArticleListActivity : BaseActivity<ActivityArticleListBinding>() {
    override fun getViewBinding(inflater: LayoutInflater): ActivityArticleListBinding {
        return ActivityArticleListBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        val cid = intent.getIntExtra("cid", 0)
        val title = intent.getStringExtra("title") ?: "体系文章"
        binding.toolbar.title = title

        if (savedInstanceState == null) {
            val fragment = ArticleListFragment().apply {
                arguments = Bundle().apply {
                    putInt("cid", cid)
                    putString("title", title)
                }
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
        binding.toolbar.setNavigationOnClickListener { finish() }

    }

    companion object {
        fun start(context: Context, cid: Int, title: String) {
            val intent = Intent(context, ArticleListActivity::class.java).apply {
                putExtra("cid", cid)
                putExtra("title", title)
            }
            context.startActivity(intent)
        }
    }
}