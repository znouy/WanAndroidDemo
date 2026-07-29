package com.example.wanandroiddemo.data.model.dto


import com.example.wanandroiddemo.data.model.domain.HotKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HotKeyDto(
    @Json(name = "id") val id: Int?,
    @Json(name = "name") val name: String?,
    @Json(name = "link") val link: String?,
    @Json(name = "order") val order: Int?,
    @Json(name = "visible") val visible: Int?
)


fun HotKeyDto.toDomain(): HotKey {
    return HotKey(
        id = id ?: 0,
        name = name.orEmpty()
    )
}