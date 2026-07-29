package com.example.wanandroiddemo.util.ext


import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

/**
 * 安全创建多级文件夹（若父级目录不存在，自动连带创建）
 */
fun File.createDirSafely(): Boolean {
    if (exists()) return isDirectory
    return mkdirs()
}

/**
 * 递归计算文件或文件夹的总大小（字节数）
 */
fun File.getTotalSize(): Long {
    if (!exists()) return 0L
    if (isFile) return length()
    var size = 0L
    listFiles()?.forEach { file ->
        size += file.getTotalSize()
    }
    return size
}

/**
 * 💡 核心：格式化文件字节数为人类直观可读的 KB / MB / GB 大小
 * 示例：1048576L.formatFileSize() -> "1.00 MB"
 */
fun Long.formatFileSize(): String {
    if (this <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(this.toDouble()) / log10(1024.0)).toInt()
    return DecimalFormat("#,##0.##").format(this / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
}

/**
 * 递归删除文件夹及内部所有子文件（常用于清空沙盒缓存目录）
 */
fun File.deleteRecursivelySafely(): Boolean {
    if (!exists()) return true
    if (isDirectory) {
        listFiles()?.forEach { file ->
            file.deleteRecursivelySafely()
        }
    }
    return delete()
}

/**
 * 传统底层流式文件复制，保障 100% 安全关闭
 */
fun File.copyToSafely(dest: File): Boolean { // TODO:     if (!exists() || isDirectory) return false
    if (!exists() || isDirectory) return false
    var input: FileInputStream? = null
    var output: FileOutputStream? = null
    return try {
        input = FileInputStream(this)
        output = FileOutputStream(dest)
        val buffer = ByteArray(1024)
        var length: Int
        while (input.read(buffer).also { length = it } > 0) {
            output.write(buffer, 0, length)
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    } finally {
        input?.close()
        output?.close()
    }
}