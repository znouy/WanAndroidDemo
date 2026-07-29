package com.example.wanandroiddemo.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.example.wanandroiddemo.databinding.ViewStateLayoutBinding

/**
 * 基于Material 3 多状态切换容器
 */
class StateLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // 使用 ViewBinding 一键解包 merge 布局，并直接挂载到当前容器 (this)
    private val binding = ViewStateLayoutBinding.inflate(
        LayoutInflater.from(context), this
    )

    // 自动识别出的真正业务内容 View
    private var contentView: View? = null

    override fun onFinishInflate() {
        super.onFinishInflate()

        // 利用 ViewBinding 属性地址进行精准比对，自动抓取用户写入的内容 View
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child != binding.layoutStateLoading &&
                child != binding.tvEmptyMsg &&
                child != binding.layoutStateError
            ) {
                contentView = child
                break
            }
        }
    }

    // =====================================================================
    // 外部控制 API（ViewBinding 极简安全控制版）
    // =====================================================================

    /**
     * 1. 切换为：显示内容（成功状态）
     */
    fun showContent() {
        binding.layoutStateLoading.visibility = GONE
        binding.tvEmptyMsg.visibility = GONE
        binding.layoutStateError.visibility = GONE
        contentView?.visibility = VISIBLE
    }

    /**
     * 2. 切换为：正在加载中
     */
    fun showLoading() {
        binding.layoutStateLoading.visibility = VISIBLE
        binding.tvEmptyMsg.visibility = GONE
        binding.layoutStateError.visibility = GONE
        contentView?.visibility = GONE
    }

    /**
     * 3. 切换为：空数据占位
     */
    fun showEmpty(message: String = "暂无相关数据哦") {
        binding.layoutStateLoading.visibility = GONE
        binding.tvEmptyMsg.visibility = VISIBLE
        binding.layoutStateError.visibility = GONE
        contentView?.visibility = GONE

        binding.tvEmptyMsg.text = message
    }

    /**
     * 4. 切换为：加载失败
     */
    fun showError(errorMsg: String = "加载失败，请检查网络后重试", onRetry: () -> Unit) {
        binding.layoutStateLoading.visibility = GONE
        binding.tvEmptyMsg.visibility = GONE
        binding.layoutStateError.visibility = VISIBLE
        contentView?.visibility = GONE

        binding.tvErrorMsg.text = errorMsg
        binding.btnRetry.setOnClickListener { onRetry() }
    }
}