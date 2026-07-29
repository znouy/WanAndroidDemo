package com.example.wanandroiddemo.data.model.domain

import com.squareup.moshi.JsonClass

/**
 * Banner数据模型
 */
@JsonClass(generateAdapter = true)
data class Banner(
    val id: Int,
    val title: String,
    val imagePath: String,
    val url: String
)