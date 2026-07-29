package com.example.wanandroiddemo.util.ext

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.TextView

//键盘收起与防抖点击


/**
 * 防止用户快速重复点击（按钮防抖限制）
 *  @param delayMillis 防抖间隔，默认 500毫秒
 *  @param onClick 点击事件回调
 */
fun View.setOnDebouncedClickListener(delayMillis: Long = 500L, onClick: (View) -> Unit) {
    setOnClickListener(object : View.OnClickListener {
        private var lastClickTime = 0L
        override fun onClick(v: View) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime >= delayMillis) {
                lastClickTime = currentTime
                onClick(v)
            }
        }
    })
}

/**
 * 强行弹出软键盘
 */
fun View.showKeyboard() {
    this.post {
        this.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        // 传入 0 代替已废弃的 SHOW_IMPLICIT，在保障功能的同时消除警告
        imm?.showSoftInput(this, 0)
    }
}

/**
 * 强行隐藏软键盘
 */
fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(this.windowToken, 0)
}

/**
 * 在 Activity 中隐藏键盘（传统方式）
 */
fun Activity.hideKeyboard() {
    val currentFocusView = this.currentFocus ?: this.window.decorView
    currentFocusView.hideKeyboard()
}

/**
 * 💡 商业实战：让 TextView 上的数字产生丝滑的滚动渐变动画
 *
 * @param targetValue 目标要达到的数值
 * @param duration 动画持续时间，默认 1000 毫秒（1秒）
 */
fun TextView.animateNumber(targetValue: Int, duration: Long = 1000L) {
    // 1. 智能起点推导：读取当前 TextView 上显示的字符串并过滤出数字，作为动画起点
    val currentValueStr = this.text.toString().filter { it.isDigit() }
    val startValue = currentValueStr.toIntOrNull() ?: 0

    // 2. 防御性处理：如果起点和终点一模一样，或者目标值为负数，无需启动动画，直接设值
    if (startValue == targetValue || targetValue < 0) {
        this.text = targetValue.toString()
        return
    }

    // 3. 核心：使用 ValueAnimator 进行区间渐变插值计算
    val animator = ValueAnimator.ofInt(startValue, targetValue)
    animator.duration = duration

    // 4. 减速插值器（DecelerateInterpolator）：
    // 产生“一开始滚得飞快，在快接近目标值时缓缓减速停下”的物理质感，视觉体验极佳
    animator.interpolator = DecelerateInterpolator()

    // 5. 监听数值渐变过程，在 UI 线程同步更新 TextView
    animator.addUpdateListener { valueAnimator ->
        val currentAnimatedValue = valueAnimator.animatedValue as Int
        this.text = currentAnimatedValue.toString()
    }

    animator.start()
}