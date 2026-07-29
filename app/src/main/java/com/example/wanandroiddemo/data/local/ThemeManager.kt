package com.example.wanandroiddemo.data.local


import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.example.wanandroiddemo.R
import timber.log.Timber

/**
 * 主题逻辑管理器
 */
object ThemeManager {
    @ColorInt
    @Volatile
    private var cachedThemeColor: Int = -1  //表示默认值（使用theme_color）@Volatile

    @Volatile
    private var cachedThemeMode: Int = -1  //表示默认值（跟随系统）

    fun getThemeColor(): Int = cachedThemeColor
    fun getThemeMode(): Int = cachedThemeMode

    fun setThemeMode(themeModel: Int) {
        cachedThemeMode = themeModel
    }

    fun setThemeColor(themeColor: Int) {
        cachedThemeColor = themeColor
    }

    /**
     * 冷启动初始化内存缓存
     */
    fun initCache(themeMode: Int, themeColor: Int) {
        cachedThemeColor = themeColor
        applyThemeMode(themeMode)
    }

    /**
     * 全局应用暗黑模式样式
     *
     * 系统在后台会自动调用 applyDayNight(),
     *
     * 如果没配置uiModel,activity触发重建,去对应的 values 或 values-night 目录重新加载颜色和资源
     *
     * 如果配置了uiModel(热刷新),activity 不会销毁重建。虽然底层自动执行了 applyDayNight()
     * 并把资源文件（Resources）切换到了 values-night，但无法隔空改变已经渲染在屏幕上的 View 的颜色
     * 需重写 onConfigurationChanged 方法，并手动把所有 View 的颜色重新赋值一遍
     */
    fun applyThemeMode(mode: Int) {
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    /**
     *
     * 同步判定当前是否处于暗黑模式环境（用于决定状态栏与控件颜色变化）
     */
    fun isNightMode(context: Context): Boolean {
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        return if (currentMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM || currentMode == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED) {
            //   情况 A：跟随系统或者未设置
            //  获取系统 UI 模式，并通过“位与(&)”运算过滤出黑夜模式相关的配置信息
            val systemNightMode =
                context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
            /// 判断系统当前是否开启了黑夜模式:黑夜返回 true,否则false
            systemNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
        } else {
            // 情况 B：APP设置了 :黑夜返回 true,否则false
            currentMode == AppCompatDelegate.MODE_NIGHT_YES
        }
    }


    /**
     * 结合当前日夜间状态，获取自适应主题颜色
     *
     * @param rawColor 用户当前设置的主题颜色（-1 代表未设置）
     */
    @ColorInt
    fun getAdaptiveThemeColor(context: Context, @ColorInt rawColor: Int): Int =
        if (isNightMode(context) || rawColor == -1) {
            Timber.e(
                "--------isNightMode:${isNightMode(context)}---rawColor：${
                    Integer.toHexString(
                        rawColor
                    )
                }---"
            )
            // 如果是夜间模式，或者用户没设置过主题颜色(-1)，直接返回 XML 定义的默认色
            ContextCompat.getColor(context, R.color.theme_color)
        } else {
            // 日间模式且用户设置过颜色，直接返回用户手动设置的主题色
            Timber.e("---------NotNightMode：rawColor：${Integer.toHexString(rawColor)} ---")
            rawColor
        }

    /**
     * 判断颜色是否属于深色（用于决定状态栏图标是黑是白）
     */
    fun isColorDark(@ColorInt color: Int): Boolean {
        val darkness =
            1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.5
    }

    /**
     * 利用 HSV 空间，对高饱和度颜色进行等比去饱和度与降暗
     */
    @ColorInt
    private fun getDesaturatedColor(@ColorInt color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[1] = (hsv[1] * 0.6f).coerceIn(0.0f, 1.0f) // 降低饱和度 40%
        hsv[2] = (hsv[2] * 0.8f).coerceIn(0.0f, 1.0f) // 降暗 20%
        return Color.HSVToColor(hsv)
    }

}