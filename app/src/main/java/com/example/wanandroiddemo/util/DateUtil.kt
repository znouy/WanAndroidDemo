package com.example.wanandroiddemo.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 *  扩展函数：将 Calendar 对象一键格式化为标准的 "yyyy-MM-dd"
 * （自动清洗内部的 0 索引月份计算，防止外部调用漏写 +1）
 */
fun Calendar.toFormattedDateString(): String {
    return String.format(
        Locale.US,
        "%d-%02d-%02d",
        get(Calendar.YEAR),
        get(Calendar.MONTH) + 1, // 内部自动 +1
        get(Calendar.DAY_OF_MONTH)
    )
}

/**
 *  顶级工具函数：将年月日整型数值一键格式化为标准的 "yyyy-MM-dd"
 * @param monthZeroIndexed 0 索引的月份（0-11，通常来自 DatePickerDialog 或是 Calendar）
 */
fun formatYearMonthDay(year: Int, monthZeroIndexed: Int, day: Int): String {
    return String.format(
        Locale.US,
        "%d-%02d-%02d",
        year,
        monthZeroIndexed + 1, // 内部自动 +1，消除外部心智负担
        day
    )
}

/**
 * 将时间戳毫秒数快速转换成指定的格式化日期
 * 使用示例：System.currentTimeMillis().toDateString("yyyy-MM-dd HH:mm")
 */
fun Long.toDateString(pattern: String = "yyyy-MM-dd HH:mm"): String {
    return try {
        val sdf = SimpleDateFormat(pattern, Locale.US)
        sdf.format(Date(this))
    } catch (e: Exception) {
        "Unknown"
    }
}