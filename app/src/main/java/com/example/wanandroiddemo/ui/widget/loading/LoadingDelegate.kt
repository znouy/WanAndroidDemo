package com.example.wanandroiddemo.ui.widget.loading

/**
 * 纯净加载控制契约，在命名上与 Kotlin 的 by 委托机制完美呼应
 */
interface LoadingDelegate {
    fun showLoading(show: Boolean)
}
