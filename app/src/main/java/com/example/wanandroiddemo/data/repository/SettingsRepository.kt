package com.example.wanandroiddemo.data.repository

import com.example.wanandroiddemo.data.local.AppPreferences
import com.example.wanandroiddemo.data.local.LocalCookieJar
import com.example.wanandroiddemo.data.local.ThemeManager
import com.example.wanandroiddemo.util.CacheManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val languageRepository: LanguageRepository,
    private val appPreferences: AppPreferences,
    private val cookieJar: LocalCookieJar, // Cookie 存储
    private val cacheManager: CacheManager
) {
    // 由仓库层向外暴露 Flow
    val settingsFlow = appPreferences.settingsFlow

    suspend fun saveThemeMode(themeMode: Int) {
        appPreferences.setThemeMode(themeMode)
    }

    suspend fun setShowBanner(show: Boolean) {
        appPreferences.setShowBanner(show)
    }

    suspend fun setShowTopArticle(show: Boolean) {
        appPreferences.setShowTopArticle(show)
    }

    suspend fun setShowBookmarkNotification(show: Boolean) {
        appPreferences.setShowBookmarkNotification(show)
    }

    suspend fun setShowQuestion(show: Boolean) {
        appPreferences.setShowQuestion(show)
    }

    suspend fun setThemeColor(color: Int) {
        ThemeManager.setThemeColor(color) //同步更新内存缓存
        // 异步持久化到磁盘
        appPreferences.setThemeColor(color)
    }
    suspend fun logout(){
        appPreferences.clearUserSession()
        appPreferences.clearUserCoin()
        cookieJar.clearCookies()
    }

    /**
     * 异步获取缓存大小
     */
    suspend fun getCacheSize(): String = cacheManager.getTotalCacheSize()

    /**
     * 异步清除缓存
     */
    suspend fun clearCache(): Boolean = cacheManager.clearAllCache()

}
