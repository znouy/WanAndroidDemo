package com.example.wanandroiddemo.data.model.dto

import com.example.wanandroiddemo.data.model.domain.Todo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TodoDto(
    @Json(name = "completeDate")
    val completeDate: String?,
    @Json(name = "completeDateStr")
    val completeDateStr: String?,
    @Json(name = "content")
    val content: String?,
    @Json(name = "date")
    val date: Long?,
    @Json(name = "dateStr")
    val dateStr: String?,
    @Json(name = "id")
    val id: Int?,
    @Json(name = "priority")// 1: 重要，2: 一般
    val priority: Int?,
    @Json(name = "status") // 0: 未完成，1: 已完成
    val status: Int?,
    @Json(name = "title")
    val title: String?,
    @Json(name = "type")
    val type: Int?,
    @Json(name = "userId")
    val userId: Int?
)

/**
 * DTO -> Domain 转换
 */
fun TodoDto.toDomain(): Todo {
    return Todo(
        id = id ?: 0,
        title = title.orEmpty(),
        content = content.orEmpty(),
        dateStr = dateStr.orEmpty(),
        isDone = status == 1,
        priority = TodoPriority.fromValue(priority?:2)
    )
}
/**
 * 优先级枚举
 */
enum class TodoPriority(val value: Int, val label: String, val colorHex: String) {
    HIGH(1, "重要", "#FF4D4F"),
    NORMAL(2, "一般", "#2f74e5");

    companion object {
        fun fromValue(value: Int): TodoPriority {
            return values().find { it.value == value } ?: NORMAL
        }
    }
}


