package com.example.wanandroiddemo.data.model.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ShareArticleDto(
    @Json(name = "shareArticles") val shareArticles: PageResponse<ArticleDto>?
)