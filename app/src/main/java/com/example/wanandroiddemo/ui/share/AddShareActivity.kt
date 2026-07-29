package com.example.wanandroiddemo.ui.share

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.wanandroiddemo.base.BaseActivity
import com.example.wanandroiddemo.databinding.ActivityAddShareBinding
import com.example.wanandroiddemo.ui.widget.loading.LoadingDelegate
import com.example.wanandroiddemo.ui.widget.loading.LoadingDelegateImpl
import com.example.wanandroiddemo.util.ext.collectLoading
import com.example.wanandroiddemo.util.ext.collectMessages
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddShareActivity : BaseActivity<ActivityAddShareBinding>(), LoadingDelegate {
    private val viewModel: AddShareViewModel by viewModels()
    override fun getViewBinding(inflater: LayoutInflater): ActivityAddShareBinding {
        return ActivityAddShareBinding.inflate(inflater)
    }

    private val loadingDelegate by lazy { LoadingDelegateImpl(supportFragmentManager) }


    override fun initView(savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { finish() }

        // 右上角确定按钮
        binding.tvSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val link = binding.etLink.text.toString().trim()

            // 提交业务
            viewModel.shareArticle(title, link)
        }
    }

    override fun initData() {
        //观察消息提示
        collectMessages(viewModel)
        //观察loading框状态
        collectLoading(viewModel.isSubmitLoading)
        //观察表单错误状态
        lifecycleScope.launch {
            viewModel.formState.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { state ->
                    binding.layoutLink.error = state.linkError
                    binding.layoutTitle.error = state.titleError
                }
        }

        // 订阅 成功/失败状态变迁
        lifecycleScope.launch {
            viewModel.uiEvent
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { event ->
                    when (event) {
                        is AddShareUiEvent.Success -> {
                            setResult(RESULT_OK)
                            finish()
                        }
                    }
                }
        }

    }

    override fun showLoading(show: Boolean) {
        loadingDelegate.showLoading(show)
        binding.tvSubmit.isEnabled = !show
        binding.tvSubmit.alpha = if (show) 0.5f else 1.0f
    }

}