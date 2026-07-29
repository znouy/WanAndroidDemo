package com.example.wanandroiddemo.data.model.dto

import com.example.wanandroiddemo.data.model.domain.Bookmark
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BookmarkDto(
    @Json(name = "id") val id: Int?,
    @Json(name = "name") val name: String?,
    @Json(name = "link") val link: String?,
    @Json(name = "order") val order: Int?,
    @Json(name = "userId") val userId: Int?,
    @Json(name = "visible") val visible: Int?
)

/**
 * DTO -> Domain 转换器
 */
fun BookmarkDto.toDomain(): Bookmark {
    return Bookmark(
        id = id ?: 0,
        name = name.orEmpty(),
        link = link.orEmpty()
    )
}