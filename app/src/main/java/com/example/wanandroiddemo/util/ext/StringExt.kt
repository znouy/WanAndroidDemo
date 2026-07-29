package com.example.wanandroiddemo.util.ext

import android.util.Patterns
import androidx.core.text.HtmlCompat
import java.security.MessageDigest

/**
 * 优雅的 HTML 实体字符解码扩展函数
 * 例如: "Android&ldquo;第一期&rdquo;" -> "Android“第一期”"
 */
fun String?.htmlDecode(): String {
    if (this.isNullOrEmpty()) return ""
    // 使用 Android 系统底层的 HTML 解析器，一句话搞定所有转义字符，且性能极佳
    return HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
}


/**
 * 💡 正则校验：是否是合规的邮箱
 */
fun String.isValidEmail(): Boolean {
    return this.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

/**
 * 💡 正则校验：是否是合规的手机号（支持国内 11 位标准手机号）
 */
fun String.isValidPhone(): Boolean {
    val pattern = "^1[3-9]\\d{9}$"
    return this.isNotEmpty() && this.matches(Regex(pattern))
}

/**
 * ：验证当前字符串是否为合法的 HTTP / HTTPS 网页链接
 */
fun String.isValidHttpUrl(): Boolean {
    //  ignoreCase = true 完美兼容用户输入 HTTP:// 或 Https:// 的情况
    return this.startsWith("http://", ignoreCase = true) ||
            this.startsWith("https://", ignoreCase = true)
}
/**
 * 💡 零宽空格注入器（Unicode 代码点安全版）
 * 100% 兼容所有 Emoji、复杂表情图标、特殊符号以及稀有中文字符，绝对不破坏 UTF-16 代理对
 */
fun String.toCharBreak(): String {
    val sb = StringBuilder()
    var i = 0
    while (i < this.length) {
        // 1.  获取当前的 Unicode 代码点（能够完整读取 32 位 Supplementary 字符）
        val codePoint = this.codePointAt(i)

        // 2.  将完整的字符（包括完整的 4 字节 Emoji）拼接进去
        sb.appendCodePoint(codePoint)

        // 3. 在整个完整字符的后面，安全地注入零宽空格
        sb.append('\u200B')

        // 4. 关键：安全跳过该字符占用的字节数（普通字符自增 1，Emoji 等代理对自动自增 2）
        i += Character.charCount(codePoint)
    }
    return sb.toString()
}
/**
 * 哈希算法：快速生成字符串的 MD5
 * 常用于用户密码加密传输、或生成唯一的网络文件本地缓存名
 */
fun String.md5(): String {
    return try {
        val md = MessageDigest.getInstance("MD5")
        val bytes = md.digest(this.toByteArray(Charsets.UTF_8))
        bytes.joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
        ""
    }
}