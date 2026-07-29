package com.example.wanandroiddemo.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.util.AttributeSet
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toUri

/**
 * 安全、防泄漏、免配置的通用 WebView
 */
class AppWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    // 方便 UI 层监听网页状态的轻量级回调（外界不需重写 WebViewClient 即可感知状态）
    var onPageStartedListener: ((url: String) -> Unit)? = null
    var onPageFinishedListener: ((url: String) -> Unit)? = null
    var onProgressChangedListener: ((progress: Int) -> Unit)? = null

    init {
        initWebSettings()
        initWebClients()
    }

    private fun initWebClients() {
        // 配置浏览器内核回调
       webChromeClient = object : WebChromeClient(){
           override fun onProgressChanged(view: WebView?, newProgress: Int) {
               super.onProgressChanged(view, newProgress)
               onProgressChangedListener?.invoke(newProgress)
           }
       }
        //在内部彻底实现“混合内容加载”与“三方 App 唤醒”安全拦截，绝不向外泄露
        webViewClient = object : WebViewClient(){
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let { onPageStartedListener?.invoke(url) }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                url?.let { onPageFinishedListener?.invoke(url) }
            }
            // Android 7.0+ 拦截
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                return handleCustomScheme(url)
            }

            // 兼容老版本系统拦截
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                val targetUrl = url ?: return false
                return handleCustomScheme(targetUrl)
            }

            private fun handleCustomScheme(url: String): Boolean {
                // 如果是标准网页地址，不拦截，返回 false 让 WebView 正常去加载
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    return false
                }

                // 如果是系统或三方私有协议（如 jianshu://, weixin://, alipays://, mailto:, tel:）
                // 使用 Intent 唤醒外部 App，并用 try-catch 建立闪退绝对防御
                try {
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // 确保运行在独立任务栈
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // 如果手机里没有安装简书（或其他对应 App），startActivity 会抛出 ActivityNotFoundException。
                    // 通过 try-catch 将其捕获并静默忽略（返回 true 拦截），
                }
                return true // 拦截事件，阻止 WebView 自行解析导致报错
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebSettings() {
        settings.apply {

            // 1. 基础配置
            javaScriptEnabled = true      // 启用 JavaScript 支持
            domStorageEnabled = true      // 启用 DOM 缓存（加载现代前端 H5 必须开启）
            databaseEnabled = true
            useWideViewPort = true        // 自适应屏幕大小
            loadWithOverviewMode = true  // 缩放至屏幕大小
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW// 允许混合加载 HTTP 和 HTTPS

            // 2. 💡 缓存策略配置（离线阅读基础）
            cacheMode = WebSettings.LOAD_DEFAULT // 默认缓存模式

            // 3. 💡 缩放与手势
            setSupportZoom(true)          // 支持缩放
            builtInZoomControls = true    // 启用内置缩放控制
            displayZoomControls = false   // 隐藏缩放控制条

            //安全配置：彻底斩断 XSS 脚本读取本地文件的通路
            allowFileAccess = false       // 禁止 WebView 访问 SD 卡和本地系统文件
            allowContentAccess = false    // 禁止通过 content:// 协议访问本地数据源
            // 禁止通过 file:// 协议在 JS 中加载其他本地文件
            // 在 Android 11 (API 30) 以下，手动关闭文件访问权限
            // 在 Android 11 及以上，系统已默认关闭，无需设置
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                @Suppress("DEPRECATION")
                allowFileAccessFromFileURLs = false
                @Suppress("DEPRECATION")
                allowUniversalAccessFromFileURLs = false
            }
        }

    }

    /**
     * 防泄漏方法：彻底释放 WebView 占用的内存与 Context 引用
     * 请在 Fragment 的 onDestroyView() 或 Activity 的 onDestroy() 中调用
     */
    fun destroySafely() {
        // 1. 移出父容器
        (parent as? ViewGroup)?.removeView(this)

        // 2. 清除内容和历史
        stopLoading()
        clearHistory()
        removeAllViews()

        // 3. 释放底层资源
        destroy()
    }
}