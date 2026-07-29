package com.example.wanandroiddemo.data.repository

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.wanandroiddemo.data.api.ApiService
import com.example.wanandroiddemo.data.local.AppPreferences
import com.example.wanandroiddemo.data.model.domain.Article
import com.example.wanandroiddemo.data.model.domain.HotKey
import com.example.wanandroiddemo.data.model.dto.toDomain
import com.example.wanandroiddemo.data.paging.SearchPagingSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

// 使用 Kotlin 扩展函数声明全局 DataStore 属性
private val Context.dataStore by preferencesDataStore(name = "search_history_pref")

@Singleton
class SearchRepository @Inject constructor(
    private val apiService: ApiService,
    private val appPreferences: AppPreferences,
    @ApplicationContext private val context: Context
) {

    val searchHistoryFlow: Flow<List<String>> = appPreferences.searchHistoryFlow

    suspend fun getHotKeys(): List<HotKey> = withContext(Dispatchers.IO) {
        val response = apiService.getHotKeys()
        if (response.errorCode == 0 && response.data != null) {
            response.data.map { it.toDomain() }
        } else {
            throw Exception(response.errorMsg.ifEmpty { "获取热门词失败" })
        }
    }

    fun getSearchResult(query: String): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            pagingSourceFactory = { SearchPagingSource(apiService, query) }
        ).flow
    }

    // 保存搜索词历史
    suspend fun saveSearchHistory(keyword: String) = withContext(Dispatchers.IO) {
        appPreferences.saveSearchHistoryKey(keyword)
    }

    // 清空历史记录
    suspend fun clearSearchHistory() = withContext(Dispatchers.IO) {
        appPreferences.clearSearchHistoryKey()
    }
}