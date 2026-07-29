package com.example.wanandroiddemo.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: Int, // WanAndroid 文章的唯一 ID 作为主键
    val title: String,
    val link: String,
    val author: String,
    val niceDate: String
)