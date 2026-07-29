package com.example.wanandroiddemo.util.ext

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.wanandroiddemo.ui.common.delegate.MessageDelegate
import com.example.wanandroiddemo.ui.widget.loading.LoadingDelegate
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 💡 针对系统原生 Fragment 的编译期安全消息收集（Collect）扩展
 * （解除了 Fragment 必须代理接口的限制，实现无感收集）
 */
fun Fragment.collectMessages(delegate: MessageDelegate) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            delegate.messageEvent.collect { message ->
                requireContext().showToast(message)
            }
        }
    }
}

/**
 * 💡 针对系统原生 Fragment 的编译期安全绑定扩展
 */
fun <T> T.collectLoading(flow: StateFlow<Boolean>) where T : Fragment, T : LoadingDelegate {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collectLatest { isShow ->
                this@collectLoading.showLoading(isShow)
            }
        }
    }
}

/**
 *  针对 Fragment 级别的安全确认弹窗
 * （在内部安全代理到宿主 Activity 身上执行，确保生命周期和窗口契约完全合规）
 */
fun Fragment.showConfirmDialog(
    title: String,
    message: String,
    positiveText: String = "确定",
    negativeText: String = "取消",
    onConfirm: () -> Unit
) {
    // 通过 requireActivity() 获取到保证绝对安全的 Activity 上下文进行弹窗
    requireActivity().showConfirmDialog(
        title = title,
        message = message,
        positiveText = positiveText,
        negativeText = negativeText,
        onConfirm = onConfirm
    )
}

