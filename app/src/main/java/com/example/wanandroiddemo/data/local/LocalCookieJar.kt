package com.example.wanandroiddemo.data.local

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalCookieJar @Inject constructor(
    @ApplicationContext context: Context
) : CookieJar {
    // 本地轻量级持久化存储
    private val sharedPreferences = context.getSharedPreferences("cookie_storage", Context.MODE_PRIVATE)

    //  内存缓存（使用 ConcurrentHashMap 确保线程安全，避免高并发请求时奔溃）
    private val cookieCache = ConcurrentHashMap<String, List<Cookie>>()

    /**
     * 自动保存服务器返回的 Cookie (如登录/注册成功时)
     */
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        // 只保存 WanAndroid 域名下的 Cookie
        if (host.contains("wanandroid.com")) {
            cookieCache[host] = cookies

            // 将 Cookie 序列化为 String 集合持久化
            val cookieStringSet = cookies.map { it.toString() }.toSet()
            sharedPreferences.edit { putStringSet(host, cookieStringSet) }
        }
    }

    /**
     * 自动为将要发出的网络请求装配 Cookie
     */
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host
        if (!host.contains("wanandroid.com")) {
            return emptyList()
        }

        //  先从内存缓存读（极快，零 IO 损耗）
        val cachedCookies = cookieCache[host]
        if (cachedCookies != null) {
            return cachedCookies
        }
        // 内存没有，再从 SharedPreferences 读，并回填到内存中
        val cookieStringSet = sharedPreferences.getStringSet(host, null) ?: return emptyList()
        val loadedCookies = cookieStringSet.mapNotNull { Cookie.parse(url, it) }

        cookieCache[host] = loadedCookies
        return loadedCookies
    }

    /**
     * 提供一个清除 Cookie 的方法（退出登录时调用）
     */
    fun clearCookies() {
        cookieCache.clear()
        sharedPreferences.edit { clear() }
    }

}