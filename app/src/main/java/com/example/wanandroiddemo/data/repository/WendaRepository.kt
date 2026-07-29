package com.example.wanandroiddemo.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.wanandroiddemo.data.api.ApiService
import com.example.wanandroiddemo.data.model.domain.Article
import com.example.wanandroiddemo.data.model.dto.toDomain
import com.example.wanandroiddemo.data.paging.WendaPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WendaRepository @Inject constructor(
    private val apiService: ApiService
) {
    fun getWendaFlow(): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 15,
                enablePlaceholders = false,
                initialLoadSize = 15
            ),
            pagingSourceFactory = { WendaPagingSource(apiService) }
        ).flow.map { pagingData ->
            // 清洗数据：在数据源边缘将 ArticleDto清洗转换成 Article (Domain)
            pagingData.map { it.toDomain() }
        }
    }
}