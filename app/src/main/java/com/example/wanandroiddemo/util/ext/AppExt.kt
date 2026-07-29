package com.example.wanandroiddemo.util.ext

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
//包管理器与应用信息

/**
 * 获取 App 的 VersionCode 兼容版本
 */
val Context.versionCode: Long
    get() = try {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
    } catch (e: PackageManager.NameNotFoundException) {
        -1L
    }

/**
 * 获取 App 的 VersionName
 */
val Context.versionName: String
    get() = try {
        packageManager.getPackageInfo(packageName, 0).versionName ?: "Unknown"
    } catch (e: PackageManager.NameNotFoundException) {
        "Unknown"
    }

/**
 * 检查特定的 App 是否在当前设备上安装
 */
fun Context.isAppInstalled(pkgName: String): Boolean {
    return try {
        packageManager.getPackageInfo(pkgName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

/**
 * 自动跳转到当前应用的系统设置详情页（常用于提示用户开启权限）
 */
fun Context.openAppSettings() {
    val intent = Intent().apply {
        action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.fromParts("package", packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}