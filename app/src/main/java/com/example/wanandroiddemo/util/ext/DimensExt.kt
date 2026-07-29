package com.example.wanandroiddemo.util.ext


import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
//屏幕度量与像素单位
/**
 * 将整型 DP 值转为 PX
 * 示例：val padding = 16.dp
 */
val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

/**
 * 将浮点型 DP 值转为 PX
 */
val Float.dp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

/**
 * 将整型 SP 值转为 PX
 */
val Int.sp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

/**
 * 获取屏幕宽度（像素）
 */
val Context.screenWidth: Int
    get() = resources.displayMetrics.widthPixels

/**
 * 获取屏幕高度（像素）
 */
val Context.screenHeight: Int
    get() = resources.displayMetrics.heightPixels

/**
 * 安全获取系统状态栏高度（像素）
 */
val Context.statusBarHeight: Int
    get() {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }