package com.example.wanandroiddemo.data.model.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 通用的网络分页包装实体
 */
@JsonClass(generateAdapter = true)
data class PageResponse<T>(
    @Json(name = "curPage") val curPage: Int?,
    @Json(name = "datas") val datas: List<T>?, // 真正的列表数据在这里
    @Json(name = "pageCount") val pageCount: Int?,
    @Json(name = "total") val total: Int?
)