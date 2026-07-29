package com.example.wanandroiddemo.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.wanandroiddemo.data.database.dao.ReadHistoryDao
import com.example.wanandroiddemo.data.model.domain.Article
import com.example.wanandroiddemo.data.model.domain.ReadHistory
import com.example.wanandroiddemo.data.model.entity.ReadHistoryEntity
import com.example.wanandroiddemo.data.model.entity.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    private val historyDao: ReadHistoryDao
) {
    // 1. 获取本地数据库分页数据流
    fun getHistoryStream(): Flow<PagingData<ReadHistory>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { historyDao.getHistoryPagingSource() }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    // 2. 插入阅读历史（在用户点击文章详情进入 Web 页时静默调用）
    suspend fun recordHistory(article: Article) = withContext(Dispatchers.IO) {
        historyDao.insertHistory(
            ReadHistoryEntity(
                id = article.id,
                title = article.title,
                link = article.link,
                author = article.author,
                niceDate = article.date,
                isArticle = article.category != "书签",
                readTimestamp = System.currentTimeMillis(),
                category = article.category
            )
        )
    }

    // 3. 删除单条历史记录
    suspend fun deleteHistory(id: Int) = withContext(Dispatchers.IO) {
        historyDao.deleteHistoryById(id)
    }

    // 4. 清空所有历史
    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        historyDao.clearAllHistory()
    }
}