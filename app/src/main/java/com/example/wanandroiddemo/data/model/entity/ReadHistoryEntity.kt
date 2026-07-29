package com.example.wanandroiddemo.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.wanandroiddemo.data.model.domain.ReadHistory
import com.example.wanandroiddemo.util.ext.toCharBreak
import com.example.wanandroiddemo.util.toDateString

@Entity(tableName = "read_history")
data class ReadHistoryEntity(
    @PrimaryKey val id: Int, // 文章 ID
    val title: String,
    val link: String,
    val author: String,
    val niceDate: String,
    val readTimestamp: Long,// 用于排序的历史阅读时间戳
    val isArticle: Boolean,
    val category: String


)

/**
 * Database Entity -> Domain History
 */
fun ReadHistoryEntity.toDomain(): ReadHistory {
    return ReadHistory(
        id = this.id,
        title = this.title. toCharBreak(),
        link = this.link,
        author = this.author,
        niceDate = this.niceDate,
        readDate = readTimestamp.toDateString(),
        isArticle = isArticle,
        category = this.category
    )
}