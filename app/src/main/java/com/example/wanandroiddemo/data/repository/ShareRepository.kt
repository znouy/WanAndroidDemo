package com.example.wanandroiddemo.data.repository


import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.wanandroiddemo.data.api.ApiService
import com.example.wanandroiddemo.data.model.domain.Article
import com.example.wanandroiddemo.data.model.dto.toDomain
import com.example.wanandroiddemo.data.paging.SharePagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareRepository @Inject constructor(
    private val apiService: ApiService
) {
    // 提供分享文章分页流
    fun getPrivateShareStream(): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { SharePagingSource(apiService) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() } // 复用通用 DTO -> Domain 转换
        }
    }

    // 删除已分享文章
    suspend fun deleteShare(id: Int) = withContext(Dispatchers.IO) {
        val response = apiService.deleteSharedArticle(id)
        if (response.errorCode != 0) {
            throw Exception(response.errorMsg.ifEmpty { "删除分享失败" })
        }
    }

    /**
     * 提交分享文章任务
     */
    suspend fun shareArticle(title: String, link: String): Unit = withContext(Dispatchers.IO) {
        val response = apiService.shareArticle(title, link)
        if (response.errorCode != 0) {
            throw Exception(response.errorMsg.ifEmpty { "分享文章失败" })
        }
    }
}
