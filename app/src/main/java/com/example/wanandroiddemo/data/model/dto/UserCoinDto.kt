package com.example.wanandroiddemo.data.model.dto

import com.example.wanandroiddemo.data.model.domain.CoinRecord
import com.example.wanandroiddemo.data.model.domain.UserCoin
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserCoinDto(
    @Json(name = "coinCount") val coinCount: Int?,
    @Json(name = "level") val level: Int?, // 当前等级
    @Json(name = "rank") val rank: String?, // 当前排名
    @Json(name = "userId") val userId: Int?,
    @Json(name = "username") val username: String?
)

@JsonClass(generateAdapter = true)
data class CoinRecordDto(
    @Json(name = "id") val id: Int?,
    @Json(name = "reason") val reason: String?, // 获取积分的原因
    @Json(name = "coinCount") val coinCount: Int?, // 获取积分的数量 (+10)
    @Json(name = "desc") val desc: String?       // 时间描述
)

// DTO -> Domain 转换器
fun CoinRecordDto.toDomain(): CoinRecord {
    val count = coinCount ?: 0
    val prefix = if (count > 0) "+$count" else "$count"
    return CoinRecord(
        id = id ?: 0,
        reason = reason.orEmpty(),
        coinCount = prefix,
        date = desc.orEmpty()
    )
}

fun UserCoinDto.toDomain(): UserCoin {
    return UserCoin(
        coinCount = coinCount ?: 0,
        level = level ?: 1, // 默认等级 1
        rank = rank.orEmpty().ifEmpty { "--" }, // 没排名时安全兜底
    )
}