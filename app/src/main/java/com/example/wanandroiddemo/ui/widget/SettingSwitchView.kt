package com.example.wanandroiddemo.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.example.wanandroiddemo.R

/**
 * 自定义设置开关项 View
 * 用于在设置界面展示带开关的列表项，支持标题和描述文本
 */
class SettingSwitchView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val switchItem: SwitchCompat
    private var onCheckedChangeListener: ((Boolean) -> Unit)? = null

    init {
        // 1. 设置 LinearLayout 属性
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val density = context.resources.displayMetrics.density
        //  设置一个最小高度（例如 56dp）
        minimumHeight = (56 * density).toInt()

        LayoutInflater.from(context).inflate(R.layout.item_setting_switch, this, true)

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvDesc = findViewById<TextView>(R.id.tvDesc)
        switchItem = findViewById(R.id.switchItem)

        // 读取自定义属性
        context.theme.obtainStyledAttributes(attrs, R.styleable.SettingItem, 0, 0).apply {
            try {
                tvTitle.text = getString(R.styleable.SettingItem_title)
                tvDesc.text = getString(R.styleable.SettingItem_desc)
            } finally {
                recycle()
            }
        }
    }

    /**
     * 设置开关状态变化的监听器
     * @param listener 回调函数，返回当前开关的状态 (isChecked)
     */
    fun setOnCheckedChangeListener(listener: (Boolean) -> Unit) {
        // 修复：必须保存到成员变量中，否则会被下面的 setChecked 方法擦除
        onCheckedChangeListener = listener
        switchItem.setOnCheckedChangeListener { _, isChecked -> listener(isChecked) }
    }

    /**
     * 设置开关状态
     * @param checked 是否开启
     * @param triggerListener 是否触发监听器回调（默认 false，避免初始化数据时意外触发业务逻辑）
     */
    fun setChecked(checked: Boolean, triggerListener: Boolean = false) {
        if (!triggerListener) {
            switchItem.setOnCheckedChangeListener(null)
            switchItem.isChecked = checked
            switchItem.setOnCheckedChangeListener { _, isChecked ->
                onCheckedChangeListener?.invoke(isChecked)
            }
        } else {
            switchItem.isChecked = checked
        }
    }

    /**
     * 获取当前开关状态
     */
    fun isChecked(): Boolean {
        return switchItem.isChecked
    }
}
