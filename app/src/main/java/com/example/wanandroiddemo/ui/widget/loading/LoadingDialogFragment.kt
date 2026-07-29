package com.example.wanandroiddemo.ui.widget.loading

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import com.example.wanandroiddemo.R

class LoadingDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_loading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 将物理窗体背景设为完全透明，使上面的 M3 圆角卡片自然浮现
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        // 2. 安全防线：阻止用户通过点击外部或按下手机物理返回键手动关闭加载框
        isCancelable = false
    }

    companion object {
        const val TAG = "GlobalLoadingDialog"
        fun newInstance() = LoadingDialogFragment()
    }
}
