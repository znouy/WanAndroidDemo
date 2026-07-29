package com.example.wanandroiddemo.ui.settings

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wanandroiddemo.R
import com.example.wanandroiddemo.base.BaseActivity
import com.example.wanandroiddemo.databinding.ActivityLanguageBinding
import com.example.wanandroiddemo.ui.about.AboutActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class LanguageActivity : BaseActivity<ActivityLanguageBinding>() {

    private val viewModel: LanguageViewModel by viewModels()
    private val languageAdapter by lazy {
        LanguageAdapter { languageItem ->
            viewModel.toggleLanguage(languageItem)
        }
    }


    override fun getViewBinding(inflater: LayoutInflater): ActivityLanguageBinding {
        return ActivityLanguageBinding.inflate(inflater)
    }



    override fun initView(savedInstanceState: Bundle?) {
        Timber.d("onCreate: Language = ${resources.configuration.locales.get(0)}")

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed() // 触发返回
        }

        //  设置 RecyclerView
        setupRecyclerView()

    }

    private fun setupRecyclerView() {
        binding.rvLanguageList.layoutManager = LinearLayoutManager(this)
        binding.rvLanguageList.adapter = languageAdapter
    }

    override fun initData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.languageList.collectLatest { list ->
                    //强制全局刷新一次，彻底解决任何 DiffUtil 拦截问题
                    languageAdapter.submitList(list)
                    languageAdapter.notifyDataSetChanged() // 绕过 DiffUtil，强制所有条目重绘

                }
            }
        }
    }

    /**
     * 当语言改变时，系统不会销毁 Activity，而是直接回调此方法。
     * 此时系统的 Resources 已经自动切换为新语言，我们只需要原地重新加载数据。
     * 解决切换语言黑屏闪烁
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // 手动更新 Toolbar 标题文字（此时系统的 resources 已经是新语言了）
        binding.toolbar.title = getString(R.string.language_title)

        //触发加载
        viewModel.loadLanguages()
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, LanguageActivity::class.java))
        }
    }
}