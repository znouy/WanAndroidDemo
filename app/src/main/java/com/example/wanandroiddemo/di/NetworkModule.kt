package com.example.wanandroiddemo.di

import android.content.Context
import android.content.pm.ApplicationInfo
import com.example.wanandroiddemo.data.api.ApiService
import com.example.wanandroiddemo.data.local.LocalCookieJar
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt 网络模块，提供 Retrofit 和 OkHttpClient
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        cookieJar: LocalCookieJar ,// 💡 Hilt 自动注入我们刚刚写好的 LocalCookieJar

        @ApplicationContext context: Context): OkHttpClient {
        // 1. 创建日志拦截器
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // 设置日志级别：
            // BODY - 打印请求和响应的 Header、Body（最详细，推荐开发阶段使用）
            // BASIC - 只打印请求类型、URL、响应状态码和耗时
            // HEADERS - 只打印请求和响应的 Header
            // 检查当前应用是否处于 Debug 模式
            val isDebug = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
            level = if (isDebug) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(Interceptor { chain ->
                val request = chain.request()
                chain.proceed(request)
            })
            .connectTimeout( 15, TimeUnit.SECONDS)//连接超时（标准 10s-15s，弱网友好）
            .readTimeout(15 ,TimeUnit.SECONDS)//读取超时（无数据包往来的静默期限制）
            .writeTimeout(15 ,TimeUnit.SECONDS)//写入（上传）超时
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder().baseUrl("https://wanandroid.com/").client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create()).build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

}