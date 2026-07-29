package com.example.wanandroiddemo.util


import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.widget.ImageViewCompat
import com.example.wanandroiddemo.R
import com.example.wanandroiddemo.data.local.ThemeManager
import timber.log.Timber

/**
 * 动态对 View 进行着色（支持任意 ARGB 颜色，零闪屏）
 */
fun View.tintBackground(color: Int) {
    backgroundTintList = ColorStateList.valueOf(color)
}

fun ImageView.tintSrc(color: Int) {
    ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(color))
}

fun TextView.tintText(color: Int) {
    setTextColor(color)
}

/**
 * 态构建一个 ColorStateList (代码版 Selector)
 * @param checkedColor 选中状态的颜色（动态主题色）
 * @param uncheckedColor 未选中状态的颜色（默认浅灰）
 */
fun Context.createThemeColorStateList(
    checkedColor: Int,
    uncheckedColor: Int = ContextCompat.getColor(this, R.color.theme_text_primary)
): ColorStateList {
    val states = arrayOf(
        intArrayOf(android.R.attr.state_checked),  // 1. 选中状态
        intArrayOf(-android.R.attr.state_checked) // 2. 未选中状态（负号代表 false）
    )
    val colors = intArrayOf(
        checkedColor,
        uncheckedColor
    )
    return ColorStateList(states, colors)
}

/**
 * 现代兼容性状态栏着色方法（兼容至最新的 Android 15 / API 35）
 */

@Suppress("DEPRECATION")
fun Activity.setStatusBarColorCompat(color: Int) {
    //  获取根据日夜间自适应的主题颜色（夜间模式下它是暗黑色）
    val finalColor = ThemeManager.getAdaptiveThemeColor(this, color)
    // 判断颜色深浅
    val isDarkBackground = ThemeManager.isColorDark(finalColor)
    Timber.d(
        "finalColor: ${Integer.toHexString(finalColor)}" +
                ",color:$color"
    )

    // 控制图标颜色（明暗自适应切换，如时间、电量、网速、Wi-Fi图标等）
    val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
    windowInsetsController.isAppearanceLightStatusBars = !isDarkBackground

    // 2. 💡 处理背景色
    if (Build.VERSION.SDK_INT < 35) { // Android 15+ (VanillaIceCream)
        // 对于 API 35 以下的旧系统，依然需要通过涂色来保证视觉一致性
        // 💡 核心保证：清除半透明标志，并允许对系统栏背景进行绘制（解决低版本国产手机着色失效的硬伤）
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        window.statusBarColor = finalColor
        // 在 Android 15+，状态栏被系统强制透明。
    }


}

@Suppress("DEPRECATION")
fun Activity.syncNightModeResources(
    isNight: Boolean
) {
    val currentMode = resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK
    val targetMode = if (isNight) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO

    // 如果发现 局部的 android.content.res.Configuration 与实际应该应用的模式不一致，强行覆写它
    if (currentMode != targetMode) {
        val newConfig = Configuration(
            resources.configuration
        ).apply {
            uiMode = (uiMode and
                    Configuration.UI_MODE_NIGHT_MASK.inv()) or targetMode
        }

        try {
            // 关键：强制让 Activity 本身的 Resources 实例应用最新的 Configuration 快照
            applyOverrideConfiguration(newConfig)
        } catch (_: IllegalStateException) {
            // 如果 getResources() 已经调用过，applyOverrideConfiguration 会抛出异常
            // 此时回退到使用 updateConfiguration 直接更新 Resources 的配置
            resources.updateConfiguration(newConfig, resources.displayMetrics)
        }
    }
}