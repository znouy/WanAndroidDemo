package com.example.wanandroiddemo.util.ext


import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.DrawableRes
import com.example.wanandroiddemo.databinding.LayoutCustomToastBinding

// 持有全局唯一的 Toast 引用
private var globalToast: Toast? = null

/**
 * 弹出高颜值的自定义样式 Toast
 * @param message 提示文本
 * @param iconRes 左侧图标的资源 ID（非必传，不传则只显示纯文本卡片）
 */
fun Context.showToast(message: String, @DrawableRes iconRes: Int? = null) {
    // 1. 立刻销毁上一次的 Toast，实现防堆叠
    globalToast?.cancel()

    // 2. 使用 ViewBinding 装载我们自定义的 XML 布局
    val binding = LayoutCustomToastBinding.inflate(LayoutInflater.from(this))

    // 3. 填充文字
    binding.tvToastMessage.text = message

    // 4. 动态控制图标：如果传了图标 ID，就展示并绑定；没传就 GONE 隐藏
    if (iconRes != null) {
        binding.ivToastIcon.setImageResource(iconRes)
        binding.ivToastIcon.visibility = View.VISIBLE
    } else {
        binding.ivToastIcon.visibility = View.GONE
    }

    // 5. 实例化原生的 Toast 容器，并将我们自定义的布局设进去
    val customToast = Toast(applicationContext).apply {
        // 设置在屏幕上的显示位置：居中靠下（Y 轴偏移 100 像素）
        setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 150)
        duration = Toast.LENGTH_SHORT

        // 将自定义 View 设为 Toast 的展示视图
        @Suppress("DEPRECATION") // 消除 API 30+ 的前台调用警告
        view = binding.root
    }

    globalToast = customToast
    customToast.show()
}