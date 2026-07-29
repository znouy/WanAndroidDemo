package com.example.wanandroiddemo.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.example.wanandroiddemo.R

/**
 * 自定义带箭头的设置项 View
 * 用于展示设置选项，点击可跳转或弹出操作，支持标题和右侧状态文本
 */
class SettingArrowView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        // 1. 设置 LinearLayout 属性
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val density = context.resources.displayMetrics.density
        //  设置一个最小高度（例如 56dp）
        minimumHeight = (56 * density).toInt()

        // 开启点击和聚焦使能（必须开启，否则水波纹不生效）
        isClickable = true
        isFocusable = true
        // 获取系统水波纹资源并设为背景
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
        setBackgroundResource(typedValue.resourceId)

        LayoutInflater.from(context).inflate(R.layout.item_setting_arrow, this, true)
        
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvValue = findViewById<TextView>(R.id.tvValue)
        
        // 读取自定义属性
        context.theme.obtainStyledAttributes(attrs, R.styleable.SettingItem, 0, 0).apply {
            try {
                tvTitle.text = getString(R.styleable.SettingItem_title)
                tvValue.text = getString(R.styleable.SettingItem_value)
            } finally {
                recycle()
            }
        }
    }

    
    /**
     * 更新右侧显示的状态文本（如：缓存大小、选中语言等）
     * @param value 要显示的新文本
     */
    fun setValue(value: String) {
        findViewById<TextView>(R.id.tvValue).text = value
    }
}
