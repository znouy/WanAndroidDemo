package com.example.wanandroiddemo.data.model.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HomeArticleDto(
    @Json(name = "curPage")
    val curPage: Int?,
    @Json(name = "datas")
    val datas: List<ArticleDto?>?,
    @Json(name = "offset")
    val offset: Int?,
    @Json(name = "over")
    val over: Boolean?,
    @Json(name = "pageCount")
    val pageCount: Int?,
    @Json(name = "size")
    val size: Int?,
    @Json(name = "total")
    val total: Int?
)

