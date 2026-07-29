package com.example.wanandroiddemo.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.wanandroiddemo.data.api.ApiService
import com.example.wanandroiddemo.data.model.domain.Article
import com.example.wanandroiddemo.data.model.dto.toDomain
import com.example.wanandroiddemo.data.paging.CollectPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectRepository @Inject constructor(
    private val apiService: ApiService
) {
    // 全局收藏状态事件广播流。Pair(文章ID, 最新收藏状态)
    // extraBufferCapacity = 5 可以防止高频点击时导致协程挂起丢帧
    private val _collectEvent = MutableSharedFlow<Pair<Int, Boolean>>(extraBufferCapacity = 5)
    val collectEvent = _collectEvent.asSharedFlow()


    /**
     * 获取我的收藏分页流
     * */
    fun getCollectFollow(): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { CollectPagingSource(apiService) }

        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
    }

    /**
     * 2. 取消收藏（在主页、广场、体系等列表页面调用）
     * @param id 文章在列表中的原生 ID（对应接口中的 originId）
     */
    suspend fun unCollectArticle(id: Int) {
        val response = apiService.uncollectArticle(id)
        if (response.errorCode == 0) {
            // 成功后，向全局广播：当前文章被取消点赞了
            _collectEvent.emit(id to false)
        } else {
            throw Exception(response.errorMsg.ifEmpty { "取消收藏失败" })
        }
    }


    /**
     * 收藏站内文章
     * @param id 文章在列表中的原生 ID
     * */
    suspend fun collectArticle(id: Int) {
        val response = apiService.collectArticle(id)
        if (response.errorCode == 0) {
            // 成功后，向全局广播：当前文章被点赞了
            _collectEvent.emit(id to true)
        } else {
            throw Exception(response.errorMsg.ifEmpty { "收藏失败" })
        }
    }
}