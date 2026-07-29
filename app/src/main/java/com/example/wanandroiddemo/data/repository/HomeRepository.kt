package com.example.wanandroiddemo.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.wanandroiddemo.data.api.ApiService
import com.example.wanandroiddemo.data.model.domain.Banner
import com.example.wanandroiddemo.data.model.domain.Article
import com.example.wanandroiddemo.data.model.dto.toDomain
import com.example.wanandroiddemo.data.paging.ArticlePagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 💡 首页仓库类，负责数据获取和处理
 *
 * `@Singleton`: 保证在整个应用生命周期内，该 Repository 只有一个单例存在。
 * `@Inject constructor`: 配合 Hilt 依赖注入，自动将 ApiService 注入到构造函数中。
 */
@Singleton
class HomeRepository @Inject constructor(
    private val apiService: ApiService
) {

    /**
     * 获取轮播图数据
     */
    suspend fun getBanners(): List<Banner> {
        val response = apiService.getBanners()
        if (response.errorCode == 0) {
            return response.data ?: emptyList()
        } else {
            throw Exception(response.errorMsg.ifEmpty { "获取轮播图失败" })
        }
    }

    /**
     * 获取置顶文章
     * */
    suspend fun getTopArticles(): List<Article> {
        val response = apiService.getTopArticles()
        return if (response.errorCode == 0) {
            response.data?.map { it.toDomain().copy(isTop = true) } ?: emptyList()
        } else {
            throw Exception(response.errorMsg.ifEmpty { "获取置顶文章失败" })
        }
    }

    fun getArticleFlow(): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { ArticlePagingSource(apiService) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

}
