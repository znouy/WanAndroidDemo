package com.example.wanandroiddemo.util

import android.content.Context
import android.os.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DecimalFormat
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.log10
import kotlin.math.pow

@Singleton
class CacheManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * 获取系统默认缓存文件夹大小 (包含内部和外部缓存)（以格式化好的字符串返回，如 "2.46MB"）
     */
    suspend fun getTotalCacheSize(): String = withContext(Dispatchers.IO) {
        //累计内部缓存大小 (/data/data/package/cache)
        var cacheSize = getFolderSize(context.cacheDir)
        // 累计外部缓存大小 (/sdcard/Android/data/package/cache)
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            context.externalCacheDir?.let {
                cacheSize += getFolderSize(it)
            }
        }
        formatFileSize(cacheSize)
    }
    /**
     * 清空本地所有缓存(清除缓存目录下的所有子文件,不删除根目录)
     */
    suspend fun clearAllCache(): Boolean = withContext(Dispatchers.IO) {
        var success = deleteDirChildren(context.cacheDir)
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            context.externalCacheDir?.let {
                success = success && deleteDirChildren(it)
            }
        }
        success
    }

    private fun getFolderSize(file: File): Long {
        return file.walkTopDown()
            .filter { it.isFile }
            .sumOf { it.length() }
    }

    private fun deleteDirChildren(dir: File?): Boolean {
        if (dir == null || !dir.isDirectory) return false
        return dir.listFiles()?.all { it.deleteRecursively() } ?: true
    }
   /**
    *  格式化文件大小输出（带两位小数）
    * */
    private fun formatFileSize(size: Long): String {
        if (size <= 0) return "0.00 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        val index = if (digitGroups >= units.size) units.size - 1 else digitGroups
        return DecimalFormat("#,##0.00").format(size / 1024.0.pow(index.toDouble())) + " " + units[index]
    }
}