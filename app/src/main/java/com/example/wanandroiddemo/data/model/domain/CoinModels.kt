package com.example.wanandroiddemo.data.model.domain

data class UserCoin(
    val coinCount: Int,
    val level: Int,
    val rank: String
)

data class CoinRecord(
    val id: Int,
    val reason: String,
    val coinCount: String, // 自动格式化为 "+10" 或者 "-5"
    val date: String
)

