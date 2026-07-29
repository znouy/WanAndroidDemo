package com.example.wanandroiddemo.data.model.dto

import com.example.wanandroiddemo.data.model.domain.User
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "id") val id: Int?,
    @Json(name = "username") val username: String?,
    @Json(name = "nickname") val nickname: String?,
    @Json(name = "publicName") val publicName: String?,
    @Json(name = "coinCount") val coinCount: Int? // 用户积分
)

// DTO -> Domain Model
fun UserDto.toDomain(): User {
    return User(
        id = id ?: 0,
        username = username.orEmpty(),
        nickname = if (!nickname.isNullOrEmpty()) nickname else publicName.orEmpty(),
        coinCount = coinCount ?: 0
    )
}