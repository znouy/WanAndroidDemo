package com.example.wanandroiddemo.util.ext

import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.cancellation.CancellationException

/**
 * 为 Kotlin 官方 Result 统一注入失败日志打印
 * @param actionName 日志标识，用于在 Logcat 中进行过滤
 */
inline fun <T> Result<T>.logOnFailure(actionName: String? = null): Result<T> {
    return onFailure { exception ->
        // 统一打印详细异常和堆栈
        Timber.e(exception, "💥 ${actionName ?: ""}")
    }
}

/**
 * 💡 1. 协程安全的异常捕获器（替代原生 runCatching）
 */
@OptIn(ExperimentalContracts::class)
/**
 * 协程安全异常捕获器
 * @param loadingFlow 自动控制加载转圈状态的 Flow（可选）
 */
inline fun <R> coRunCatching(
    loadingFlow: MutableStateFlow<Boolean>? = null,
    block: () -> R
): Result<R> {
    contract {//声明锲约：保证这个 block Lambda 绝不会泄漏到别处去（callsInPlace）
        // 且在这个函数执行期间，有且仅会被执行一次（EXACTLY_ONCE）
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    //  启动时自动开启转圈
    loadingFlow?.value = true
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e // 协程取消异常必须抛出，确保协程顺利安全退出
    } catch (e: Throwable) {
        Result.failure(e) // 业务与网络异常安全捕获
    } finally {
        // 不管是成功、崩溃、还是协程中途被取消,都将loading取消
        loadingFlow?.value = false
    }


}

/**
 * 自动对 Result 的 failure 状态进行网络异常拦截与语义转换
 */
fun <T> Result<T>.mapNetworkException(): Result<T> {
    return if (this.isFailure) {
        val originalException = this.exceptionOrNull() ?: return this
        // 封装成新 Exception，并将原始异常作为 cause 传入，绝不丢失堆栈轨迹
        val translatedException =
            Exception(originalException.toUserFriendlyMessage(), originalException)
        Result.failure(translatedException)
    } else {
        this
    }
}