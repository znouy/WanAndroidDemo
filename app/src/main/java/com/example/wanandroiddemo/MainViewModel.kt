package com.example.wanandroiddemo

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanandroiddemo.data.local.ThemeManager
import com.example.wanandroiddemo.data.model.domain.UserCoin
import com.example.wanandroiddemo.data.repository.AuthRepository
import com.example.wanandroiddemo.data.repository.CoinRepository
import com.example.wanandroiddemo.data.repository.SettingsRepository
import com.example.wanandroiddemo.util.ext.coRunCatching
import com.example.wanandroiddemo.util.ext.logOnFailure
import com.example.wanandroiddemo.util.ext.mapNetworkException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val coinRepository: CoinRepository
) : ViewModel() {

    // 主页只订阅它关心的设置项和会话，数据源统一来自底层的单例 appPreferences
    val settingsConfig = settingsRepository.settingsFlow.shareIn(//stateIn区别：不需要设置默认值
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),//彻底离开或关闭界面时，5 秒倒计时才会停止订阅
        replay = 1 // 保证新订阅者能收到最新的配置，在没有读到 DataStore 数据前保持静默，读到后才发射
    )

    val userSession = authRepository.userSessionFlow.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        replay = 1 // 缓存最新的一次发射值，新订阅者能立即收到。在没有读到 DataStore 数据前保持静默，读到后才发射
    )

    //  【被动响应观察】：始终只观察单一本地缓存，UI 变动全部来源于这个冷流的变换
    val userCoinInfo: StateFlow<UserCoin?> = coinRepository.localCoinFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )
    val showQuestion: Flow<Boolean> = settingsRepository.settingsFlow.map { it.showQuestion }

    init {
        //当主页检测到用户登录成功时，自动静默同步一次数据刷入本地
        Timber.d("------------------------")
        viewModelScope.launch {
            userSession.collectLatest { session ->
                Timber.d("-----------session.isLogin=${session.isLogin}-------------")
                if (session.isLogin) {
                    fetchUserCoin()
                }
            }
        }
    }

    /**
     * 侧滑菜单点击切换夜间模式

     * 该方法首先通过 [ThemeManager] 获取当前的夜间模式状态，并根据当前状态自动决定切换到的目标模式。
     * 同时，将新的模式设置持久化保存到 AppPreferences 中，并立即应用主题更改。
     *
     * @param context 当前的上下文，用于检查当前主题模式。
     * @param mode 目标主题模式，通常为 [AppCompatDelegate.MODE_NIGHT_NO]、
     *             [AppCompatDelegate.MODE_NIGHT_YES] 或 [AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM]。
     *
     */
    fun toggleThemeMode(context: Context) {
        val isCurrentlyNight = ThemeManager.isNightMode(context)
        val targetThemeMode = if (isCurrentlyNight) {
            AppCompatDelegate.MODE_NIGHT_NO
        } else {
            AppCompatDelegate.MODE_NIGHT_YES
        }
        //同步更新内存缓存
        ThemeManager.setThemeMode(targetThemeMode)
        // 应用模式样式
        ThemeManager.applyThemeMode(targetThemeMode)

        viewModelScope.launch {
            settingsRepository.saveThemeMode(targetThemeMode)
        }
    }

    fun logout() {
        viewModelScope.launch {
            settingsRepository.logout()
        }
    }

    /**
     * 静默拉取积分等级排名
     */
    private fun fetchUserCoin() {
        viewModelScope.launch {
            coRunCatching {
                coinRepository.getUserCoin()
            }.logOnFailure("获取积分等级失败")
                .mapNetworkException()
        }
    }
}