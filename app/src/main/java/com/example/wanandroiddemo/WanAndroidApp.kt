package com.example.wanandroiddemo

import android.app.Application
import com.example.wanandroiddemo.data.local.AppPreferences
import com.example.wanandroiddemo.data.local.ThemeManager
import com.example.wanandroiddemo.util.LogManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * 应用入口，初始化 Hilt。
 */
@HiltAndroidApp
class WanAndroidApp : Application() {
    @Inject
    lateinit var appPreferences: AppPreferences
    override fun onCreate() {
        super.onCreate()
        LogManager.init(this)

        initThemeColorAndMode()

    }

    /**
     * 获取主题和日夜模式并应用
     * */
    private fun initThemeColorAndMode() {
        // 确保冷启动时同步读取并应用
        val settingsConfig = runBlocking {
            appPreferences.settingsFlow.first()
        }
        //获取暗黑模式并应用到系统（存内存）
        ThemeManager.initCache(settingsConfig.themeModel, settingsConfig.themeColor)
    }
}
