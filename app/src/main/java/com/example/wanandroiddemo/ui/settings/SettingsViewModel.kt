package com.example.wanandroiddemo.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanandroiddemo.data.local.ThemeManager
import com.example.wanandroiddemo.data.repository.AuthRepository
import com.example.wanandroiddemo.data.repository.LanguageRepository
import com.example.wanandroiddemo.data.repository.SettingsRepository
import com.example.wanandroiddemo.ui.common.delegate.MessageDelegate
import com.example.wanandroiddemo.util.ext.coRunCatching
import com.example.wanandroiddemo.util.ext.logOnFailure
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val languageRepository: LanguageRepository,
    private val messageDelegate: MessageDelegate
) : ViewModel(),MessageDelegate by messageDelegate {
    private val _cacheSize = MutableStateFlow<String>("0KB")
    val cacheSize: StateFlow<String> = _cacheSize

    val isActionLoading = MutableStateFlow(false)

    val settingsConfig = repository.settingsFlow.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        replay = 1
    )
    val userSession = authRepository.userSessionFlow.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        replay = 1
    )
    init {
        loadCacheSize()
    }
    fun getCurrentLanguageRes() = languageRepository.getCurrentLanguageNameRes()
    fun setShowBanner(show: Boolean) {
        viewModelScope.launch { repository.setShowBanner(show) }
    }

    fun setShowTopArticle(show: Boolean) {
        viewModelScope.launch { repository.setShowTopArticle(show) }
    }

    fun setShowBookmarkNotification(show: Boolean) {
        viewModelScope.launch { repository.setShowBookmarkNotification(show) }
    }

    fun setShowQuestion(show: Boolean) {
        viewModelScope.launch { repository.setShowQuestion(show) }
    }

    fun setThemeColor(color: Int) {
        ThemeManager.setThemeColor(color) //同步更新内存缓存
        // 异步持久化到磁盘
        viewModelScope.launch { repository.setThemeColor(color) }
    }


    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    /**
     * 异步获取缓存大小
     */
    fun loadCacheSize() {
        viewModelScope.launch {
            coRunCatching {
                repository.getCacheSize()
            }.logOnFailure("getCacheSize failure")
                .onSuccess { size ->
                    _cacheSize.value = size
                }
        }
    }

    /**
     * 异步清除缓存
     */
    fun clearCache() {
        viewModelScope.launch {
            coRunCatching(isActionLoading) {
                repository.clearCache()
            }.logOnFailure("clearCache failure")
                .onSuccess {
                    emitMessage("清除缓存成功")
                    loadCacheSize() // 重新读取，此时应该会自动更新并显示为 "0.00B"
                }
                .onFailure { exception ->
                    emitMessage(exception.message ?: "清除缓存失败")
                }
        }
    }
}
