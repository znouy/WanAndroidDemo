package com.example.wanandroiddemo.ui.common

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.example.wanandroiddemo.data.local.AppPreferences
import com.example.wanandroiddemo.ui.auth.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthGuard @Inject constructor(
    private val appPreferences: AppPreferences
) {

    fun startWith(scope: CoroutineScope, context: Context, targetIntent: Intent) {
        scope.launch {

            val isLogin = appPreferences.userSessionFlow.first().isLogin
            if (isLogin) {
                context.startActivity(targetIntent)
            } else {
                context.startActivity(Intent(context, LoginActivity::class.java))
            }
        }
    }
    fun startWith(scope: CoroutineScope, context: Context, action: ()-> Unit) {
        scope.launch {

            val isLogin = appPreferences.userSessionFlow.first().isLogin
            if (isLogin) {
                // 已登录：直接执行跳转动作
                action()
            } else {
                context.startActivity(Intent(context, LoginActivity::class.java))
            }
        }
    }


    // 用于处理“需要回调结果”的登录拦截跳转
    fun startWith(
        scope: CoroutineScope,
        context: Context,
        launcher: ActivityResultLauncher<Intent>, // 传入注册好的 launcher
        targetIntent: Intent
    ) {
        scope.launch {
            val isLogin = appPreferences.userSessionFlow.first().isLogin
            if (isLogin) {
                // 登录成功，使用具有回调感知能力的 launcher 启动
                launcher.launch(targetIntent)
            } else {
                // 未登录，跳转至登录页面
                context.startActivity(Intent(context, LoginActivity::class.java))
            }
        }
    }
}