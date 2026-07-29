package com.example.wanandroiddemo.util.ext


import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.wanandroiddemo.ui.common.delegate.MessageDelegate
import com.example.wanandroiddemo.ui.widget.loading.LoadingDelegate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 针对系统原生 ComponentActivity 的编译期安全消息收集（Collect）扩展
 */
fun ComponentActivity.collectMessages(delegate: MessageDelegate) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            delegate.messageEvent.collect { message ->
                // 只有在最底层的 UI 渲染时，我们才决定是用 Toast、SnackBar 还是弹窗展示
                showToast(message)
            }
        }
    }
}

/**
 * 订阅LoadingFlow并调用 showLoading() 弹加载窗
 *  针对系统原生 ComponentActivity 的编译期安全绑定扩展
 */
fun <T> T.collectLoading(flow: StateFlow<Boolean>) where T : ComponentActivity, T : LoadingDelegate {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collectLatest { isShow ->
                this@collectLoading.showLoading(isShow)
            }
        }
    }
}

/**
 *  针对 Activity 级别的安全确认弹窗
 * （Receiver 限制为 Activity，从物理上隔绝了在 ApplicationContext 中调用的可能 🚫）
 */
fun Activity.showConfirmDialog(
    title: String,
    message: String,
    positiveText: String = "确定",
    negativeText: String = "取消",
    onConfirm: () -> Unit
) {
    MaterialAlertDialogBuilder(this) // 此处的 this 是 Activity，保证持有 WindowToken 和主题属性 [4, 5]
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveText) { dialog, _ ->
            onConfirm()
            dialog.dismiss()
        }
        .setNegativeButton(negativeText) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}
