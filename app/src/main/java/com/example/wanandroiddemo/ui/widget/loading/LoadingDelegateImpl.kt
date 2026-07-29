package com.example.wanandroiddemo.ui.widget.loading

import androidx.fragment.app.FragmentManager

/**
 * 独立的加载表现层代理，不与任何具体的 Activity/Fragment 产生继承耦合
 */
class LoadingDelegateImpl(
    private val fragmentManager: FragmentManager
) : LoadingDelegate {

    private var loadingDialog: LoadingDialogFragment? = null

    override fun showLoading(show: Boolean) {
        if (show) {
            if (loadingDialog == null) {
                loadingDialog = LoadingDialogFragment.newInstance()
            }
            // 判断 isAdded 避免重复添加，并验证 isStateSaved 确保宿主状态安全
            if (!loadingDialog!!.isAdded && !fragmentManager.isStateSaved) {
                loadingDialog!!.show(fragmentManager, LoadingDialogFragment.TAG)
            }
        } else {
            // 利用 dismissAllowingStateLoss 替换 dismiss，
            // 彻底杜绝由于协程在后台线程执行完毕并尝试 dismiss 时，宿主已被用户退后台导致的 StateLoss 闪退崩溃。
            loadingDialog?.dismissAllowingStateLoss()
            loadingDialog = null
        }
    }
}
