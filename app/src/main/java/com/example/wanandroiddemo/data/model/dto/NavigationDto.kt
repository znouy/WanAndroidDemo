package com.example.wanandroiddemo.data.model.dto

import com.example.wanandroiddemo.data.model.domain.NavigationData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NavigationDto(
    @Json(name = "cid") val cid: Int?,
    @Json(name = "name") val name: String?,
    @Json(name = "articles") val articles: List<ArticleDto>?
)

// 导航层级的转换
fun NavigationDto.toDomain(): NavigationData {
    return NavigationData(
        cid = cid ?: 0,
        name = name.orEmpty(),
        // 如果 articles 列表为 null，则返回空列表；否则循环把每一个 ArticleDto 转换为 Domain
        articles = articles?.map { it.toDomain() }.orEmpty()
    )
}
