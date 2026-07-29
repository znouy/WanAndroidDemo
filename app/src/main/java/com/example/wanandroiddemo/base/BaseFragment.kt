package com.example.wanandroiddemo.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * 基础 Fragment，集成 ViewBinding。
 */
abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    //双属性设计
    // 为了用 _binding（可空 var）来在 onDestroyView 时安全释放内存防止泄漏；
    // 用 binding（非空 val 的 Getter）来让子类写代码时免去写 ? 和 !! 的痛苦，实现安全与优雅的完美兼得
    private var _binding: VB? = null
    protected val binding: VB get() = _binding!!

    abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = getViewBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
    }

    /**
     * 子类实现：初始化控件、设置监听器等
     */
    abstract fun initView()

    /**
     * 子类实现：发起网络请求、观察 LiveData/Flow 等
     */
    abstract fun initData()

    /**
     * 💡 Fragment 的视图销毁时，必须将 Binding 引用置空。
     * 否则，当 Fragment 被退入后台栈（BackStack）时，其持有的整个 View 树都无法被 GC 回收，导致内存泄漏。
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
