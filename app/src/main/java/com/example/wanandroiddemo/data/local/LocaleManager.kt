package com.example.wanandroiddemo.data.local

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 管理应用程序的语言区域（Locale）设置。
 *
 * 该类提供了获取系统默认语言名称以及当前应用程序选中的语言标签的功能。
 * 它利用 [AppCompatDelegate] 来处理多语言切换，支持“跟随系统”或特定语言设置。
 */
@Singleton
class LocaleManager @Inject constructor() {
    /**
     * 获取当前手机系统默认语言的本地化名称。
     * 例如：在中文系统下返回 "中文"，在英文系统下返回 "English"。
     *
     * @return 系统默认语言的显示名称。
     */
    fun getSystemDefaultLanguageName(): String {
        val systemLocale = Locale.getDefault()
        return systemLocale.getDisplayName(systemLocale)
    }

    /**
     * 获取当前应用程序已设置的语言标签（Language Tag）。
     *
     * @return 返回语言标签字符串（如 "zh-CN", "en-US" 等）。
     * 如果返回空字符串 ""，则表示当前处于“跟随系统”状态。
     */ fun getCurrentLanguageTag(): String {
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        return if (currentLocales.isEmpty) {
            "" // 如果为空，说明处于“跟随系统”状态
        } else {
            currentLocales.get(0)?.toLanguageTag() ?: ""
        }
    }

    /**
     * 修改应用程序的语言环境。
     *
     * 该方法调用 [AppCompatDelegate.setApplicationLocales] 将语言设置持久化到框架层。
     * 系统会自动处理 Activity 的重建（如果配置了 configChanges）或重新启动以应用新语言。
     *
     * @param tag 语言标签字符串。传入空字符串 "" 表示恢复为“跟随系统”默认行为。
     */
    fun setAppLanguage(tag: String) {
        val localeList = if (tag.isEmpty()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(tag)
        }
        // 执行底层切换
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}