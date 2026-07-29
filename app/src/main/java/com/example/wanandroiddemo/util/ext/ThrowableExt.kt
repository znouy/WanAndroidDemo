package com.example.wanandroiddemo.util.ext

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 将底层的物理异常/网络异常翻译成可读的提示语
 */
fun Throwable.toUserFriendlyMessage(): String {
    return when (this) {
        is SocketTimeoutException -> "网络连接超时，请检查您的网络后重试"
        is ConnectException -> "无法连接到服务器，请确保您的手机已联网"
        is UnknownHostException -> "域名解析失败，请检查网络设置或稍后再试"
        is HttpException -> {
            when (this.code()) {
                400 -> "错误请求，请检查参数 (400)"
                403 -> "服务器拒绝请求 (403)"
                404 -> "您访问的页面不存在 (404)"
                500 -> "服务器开小差了，技术人员正在抢修 (500)"
                503 -> "系统维护中，请稍后再试"
                else -> "服务器响应异常，错误码: ${this.code()}"
            }
        }
        is JsonDataException, is JsonEncodingException -> "数据解析异常，请联系客服处理"
        else -> this.message ?: "网络出了点小状况，请稍后再试"
    }
}