package com.example.wanandroiddemo.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wanandroiddemo.data.model.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    // 1. 从数据库读取文章列表。使用 Flow，当数据库有变动时会自动向 UI 推送最新数据
    @Query("SELECT * FROM articles")
    fun getAllArticles(): Flow<List<ArticleEntity>>

    // 2. 写入文章列表。OnConflictStrategy.REPLACE 代表如果 ID 重复就覆盖
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)

    // 3. 清空本地文章缓存
    @Query("DELETE FROM articles")
    suspend fun clearAllArticles()
}