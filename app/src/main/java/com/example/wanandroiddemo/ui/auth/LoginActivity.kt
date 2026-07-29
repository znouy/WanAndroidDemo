package com.example.wanandroiddemo.ui.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.wanandroiddemo.base.BaseActivity
import com.example.wanandroiddemo.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    // 💡 使用 activity-ktx 的 by viewModels() 代理委托创建 ViewModel
    private val viewModel: AuthViewModel by viewModels()
    private var isRegisterMode = false // 是否是注册模式状态标识

    override fun getViewBinding(inflater: LayoutInflater): ActivityLoginBinding {
        return ActivityLoginBinding.inflate(inflater)
    }


    override fun initView(savedInstanceState: Bundle?) {
        // 1. 提交按钮点击事件
        binding.btnSubmit.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (isRegisterMode) {
                val repassword = binding.etRePassword.text.toString().trim()
                viewModel.register(username, password, repassword)
            } else {
                viewModel.login(username, password)
            }
        }

        // 2. 登录/注册模式无缝切换
        binding.tvSwitchMode.setOnClickListener {
            isRegisterMode = !isRegisterMode
            if (isRegisterMode) {
                binding.layoutRePassword.isVisible = true
                binding.btnSubmit.text = "注册"
                binding.tvSwitchMode.text = "已有账号？立即登录"
            } else {
                binding.layoutRePassword.isVisible = false
                binding.btnSubmit.text = "登录"
                binding.tvSwitchMode.text = "没有账号？立即注册"
            }
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
    }


    override fun initData() {
        // 3. Activity 的生命周期安全收集 Flow
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    renderUi(state)
                }
            }
        }
    }

    private fun renderUi(state: AuthUiState) {
        // 控制进度条显示
        binding.progressBar.isVisible = state is AuthUiState.Loading

        // 登录/注册时禁用输入框和按钮，防止二次点击
        val enableInputs = state !is AuthUiState.Loading
        binding.etUsername.isEnabled = enableInputs
        binding.etPassword.isEnabled = enableInputs
        binding.etRePassword.isEnabled = enableInputs
        binding.btnSubmit.isEnabled = enableInputs

        when (state) {
            is AuthUiState.Success -> {
                Toast.makeText(this, "欢迎回来: ${state.user.nickname}", Toast.LENGTH_SHORT).show()
                // 登录成功逻辑：设置 Result 回执并关闭当前页面
                setResult(RESULT_OK)
                finish()
            }

            is AuthUiState.Error -> {
                Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
            }

            else -> {}
        }
    }

    companion object {
        /**
         * 启动登录页面的通用方法
         * @param context 启动上下文（可以是 Activity、Fragment 或 Application）
         */
        @JvmStatic
        fun start(context: Context) {
            val intent = Intent(context, LoginActivity::class.java).apply {
                // 如果传入的 context 不是 Activity（例如是 ApplicationContext），
                // 必须添加 FLAG_ACTIVITY_NEW_TASK 标记，否则系统会崩溃报错。
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(intent)
        }
    }
}