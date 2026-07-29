package com.example.wanandroiddemo.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import timber.log.Timber

/**
 * 全局日志管理中心（自包含单文件版本）
 */
object LogManager {

    /**
     * 在 Application 中调用，初始化日志配置
     */
    fun init(context: Context) {
        // 利用系统 ApplicationInfo 动态判断当前是 Debug 还是 Release
        val isDebug = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        if (isDebug) {
            Timber.plant(MethodDebugTree())
        } else {
            // 💡 直接实例化内部的私有 ReleaseTree
            Timber.plant(ReleaseTree())
        }
    }

    /**
     * 当用户登录成功后，绑定用户 ID
     */
    fun bindUserId(userId: String) {
        Timber.i("成功绑定用户日志系统，UserID: %s", userId)
        // 💡 绑定到第三方监控平台
        // CrashReport.setUserId(userId)
    }
    // =====================================================================
    // 自定义DebugTree，实现可以显示调用的方法名
    // =====================================================================
    /**
     * 商业实战：自定义 Timber 树，自动在 TAG 中注入 [文件名:行号]#方法名
     */
    class MethodDebugTree : Timber.DebugTree() {

        override fun createStackElementTag(element: StackTraceElement): String? {
            // 💡 核心：element 包含了调用 Log 的所有堆栈信息
            return String.format(
                "(%s:%s)#%s()",
                element.fileName,     // 文件名，如 MainActivity.kt
                element.lineNumber,   // 行号，如 24
                element.methodName    // 方法名，如 initView
            )
        }
    }

    // =====================================================================
    // 💡 内部私有组件：将 ReleaseTree 声明为私有内部类，实现高内聚，防止外部篡改
    // =====================================================================

    private class ReleaseTree : Timber.Tree() {

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            // 1. 过滤低级别日志
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }

            // 2. 严重的 ERROR 和 WARN 级别的日志，静默上报到云端
            if (priority == Log.ERROR || priority == Log.WARN) {
                reportToCrashPlatform(priority, tag, message, t)
            }

            // 3. 将关键日志写入本地加密文件
            writeToLocalLogFile(priority, tag, message)
        }

        /**上报异常到崩溃分析平台*/
        private fun reportToCrashPlatform(
            priority: Int,
            tag: String?,
            message: String,
            t: Throwable?
        ) {
            val exception = t ?: Exception("[$tag] $message")
            // CrashReport.postCatchedException(exception)
//            CrashReport.postCatchedException(t ?: Exception("[$tag] $message"))
        }

        /**本地加密日志存储 ,可以增加一个上传日志上报到服务器*/
        private fun writeToLocalLogFile(priority: Int, tag: String?, message: String) {
            // Logan.w("[$tag] $message", priority)
        }
    }


}