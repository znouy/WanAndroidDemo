package com.example.wanandroiddemo.data.model.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BaseResponse<T>(
    val data: T?, // TODO: 可能为空
    val errorCode: Int,
    val errorMsg: String
)
