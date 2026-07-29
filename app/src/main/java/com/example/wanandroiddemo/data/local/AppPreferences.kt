package com.example.wanandroiddemo.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.wanandroiddemo.data.model.domain.UserCoin
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

//本质上是一个本地数据源（Local DataSource）的包装类
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    companion object {
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")

        // 用户会话相关
        val IS_LOGIN = booleanPreferencesKey("is_login")
        val USER_ID = intPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_HEAD = stringPreferencesKey("user_head")

        // 系统设置
        val SHOW_BANNER = booleanPreferencesKey("show_banner")
        val SHOW_TOP_ARTICLE = booleanPreferencesKey("show_top_article")
        val SHOW_BOOKMARK_NOTIFICATION = booleanPreferencesKey("show_bookmark_notification")
        val SHOW_QUESTION = booleanPreferencesKey("show_question")
        val THEME_COLOR = intPreferencesKey("theme_color")
        val THEME_MODE = intPreferencesKey("theme_model")

        //搜索历史记录
        val SEARCH_HISTORY_KEYS = stringSetPreferencesKey("search_history_keys")

        //UserCoin
        val COIN_COUNT = intPreferencesKey("coin_count")
        val LEVEL = intPreferencesKey("level")
        val RANK = stringPreferencesKey("rank")
    }


    val settingsFlow: Flow<SettingsConfig> = context.dataStore.data
        .catch { exception ->//Flow.catch 绝不拦截 CancellationException
            if (exception is IOException) emit(emptyPreferences()) else {
                Timber.e(exception, "DataStore 发生了非 IO 异常")
                emit(emptyPreferences()) // 线上安全兜底
            }
        }.map { prefs ->
            SettingsConfig(
                isFirstLaunch = prefs[IS_FIRST_LAUNCH] ?: false,
                showBanner = prefs[SHOW_BANNER] ?: false,
                showTopArticle = prefs[SHOW_TOP_ARTICLE] ?: false,
                showBookmarkNotification = prefs[SHOW_BOOKMARK_NOTIFICATION] ?: false,
                showQuestion = prefs[SHOW_QUESTION] ?: false,
                themeColor = prefs[THEME_COLOR] ?: -1,
                themeModel = prefs[THEME_MODE] ?: -1

            )
        }
    val userSessionFlow: Flow<UserSession> = context.dataStore.data.catch { exception ->
        if (exception is IOException) emit(emptyPreferences()) else {
            Timber.e(exception, "DataStore 发生了非 IO 异常")
            emit(emptyPreferences()) // 线上安全兜底
        }
    }.map { prefs ->
        UserSession(
            isLogin = prefs[IS_LOGIN] ?: false,
            userId = prefs[USER_ID] ?: -1,
            userName = prefs[USER_NAME].orEmpty(),
            userHead = prefs[USER_HEAD].orEmpty(),
        )
    }
    val localCoinFlow: Flow<UserCoin?> = context.dataStore.data.catch { exception ->
        if (exception is IOException) emit(emptyPreferences()) else {
            Timber.e(exception, "DataStore 发生了非 IO 异常")
            emit(emptyPreferences()) // 线上安全兜底
        }
    }.map { prefs ->
        val coinCount = prefs[COIN_COUNT]
        val level = prefs[LEVEL]
        val rank = prefs[RANK]
        if (coinCount == null || level == null) {
            return@map null
        }
        UserCoin(coinCount, level, rank.orEmpty())
    }

    /**
     * 响应式获取历史搜索词列表
     */
    val searchHistoryFlow: Flow<List<String>> = context.dataStore.data.map { preferences ->
        preferences[SEARCH_HISTORY_KEYS]?.toList()?.reversed() ?: emptyList() // 倒序，保证最新的在前面
    }

    suspend fun setShowBanner(show: Boolean) {
        val edit = context.dataStore.edit { it[SHOW_BANNER] = show }
        Timber.e("---edit--------$edit")
    }

    suspend fun setShowTopArticle(show: Boolean) {
        context.dataStore.edit { it[SHOW_TOP_ARTICLE] = show }
    }

    suspend fun setShowBookmarkNotification(show: Boolean) {
        context.dataStore.edit { it[SHOW_BOOKMARK_NOTIFICATION] = show }
    }

    suspend fun setShowQuestion(show: Boolean) {
        context.dataStore.edit { it[SHOW_QUESTION] = show }
    }

    suspend fun setThemeColor(color: Int) {
        context.dataStore.edit { it[THEME_COLOR] = color }
    }

    suspend fun setThemeMode(mode: Int) {
        val edit = context.dataStore.edit { it[THEME_MODE] = mode }
        Timber.d("-------edit------$edit")
    }

    suspend fun setFirstLaunch(isFirst: Boolean) {
        context.dataStore.edit { it[IS_FIRST_LAUNCH] = isFirst }
    }


    /**
     * 更新本地缓存的积分、排名、等级
     */

    suspend fun saveUserCoin(userCoin: UserCoin) {
        context.dataStore.edit {
            it[COIN_COUNT] = userCoin.coinCount
            it[LEVEL] = userCoin.level
            it[RANK] = userCoin.rank
        }
    }

    suspend fun clearUserCoin() {
        context.dataStore.edit {
            it.remove(COIN_COUNT)
            it.remove(LEVEL)
            it.remove(RANK)
        }
    }

    /**
     * 💡 保存用户登录会话（登录成功时调用）
     */
    suspend fun setUserSession(
        isLogin: Boolean,
        id: Int = -1,
        userName: String = "未登录",
        userHead: String = "",
    ) {
        context.dataStore.edit {
            it[IS_LOGIN] = isLogin
            it[USER_ID] = id
            it[USER_NAME] = userName
            it[USER_HEAD] = userHead
        }
    }

    /**
     * 💡 一键清除用户登录会话（退出登录时调用）
     */
    suspend fun clearUserSession() {
        context.dataStore.edit {
            it[IS_LOGIN] = false
            it[USER_ID] = -1
            it[USER_HEAD] = ""
            it[USER_NAME] = ""
        }
    }

    /**
     * 清空搜索历史
     */
    suspend fun clearSearchHistoryKey() {
        context.dataStore.edit { it.remove(SEARCH_HISTORY_KEYS) }
    }

    /**
     * 保存搜索词到历史记录（自动去重、并移到最前）
     */
    suspend fun saveSearchHistoryKey(keyword: String) {
        if (keyword.isBlank()) return
        context.dataStore.edit { preferences ->
            val currentHistory = preferences[SEARCH_HISTORY_KEYS]?.toMutableSet() ?: mutableSetOf()
            // 排重，保证唯一的搜索词位置保持最新
            currentHistory.remove(keyword)
            currentHistory.add(keyword)
            preferences[SEARCH_HISTORY_KEYS] = currentHistory
        }
    }


}

data class SettingsConfig(
    val isFirstLaunch: Boolean = false,
    val showBanner: Boolean = false,
    val showTopArticle: Boolean = false,
    val showBookmarkNotification: Boolean = false,
    val showQuestion: Boolean = false,
    val themeColor: Int = -1, // 不设用默认系统主题色
    val themeModel: Int = -1 //MODE_NIGHT_FOLLOW_SYSTEM = -1;
)

data class UserSession(
    val isLogin: Boolean = false,
    val userId: Int = -1,
    val userName: String = "",
    val userHead: String = "",
)

