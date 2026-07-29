package com.example.wanandroiddemo.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.wanandroiddemo.data.api.ApiService
import com.example.wanandroiddemo.data.model.dto.ArticleDto
import com.example.wanandroiddemo.util.ext.coRunCatching
import com.example.wanandroiddemo.util.ext.logOnFailure
import com.example.wanandroiddemo.util.ext.mapNetworkException


// 专用于分享列表的网络 Paging 3 数据源
class SharePagingSource(
    private val apiService: ApiService
) : PagingSource<Int, ArticleDto>() {

    override fun getRefreshKey(state: PagingState<Int, ArticleDto>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArticleDto> {
        val page = params.key ?: 1 // 玩Android 的分享接口页码从 1 开始
        val result = coRunCatching { apiService.getPrivateArticles(page) }
            .logOnFailure("获取分享文章列表失败")
            .mapNetworkException()

        return result.fold(onSuccess = {
            val data = it.data?.shareArticles
            val dataList = data?.datas ?: emptyList()
            LoadResult.Page(
                data = dataList,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (page > (data?.pageCount ?: 0)) null else page + 1
            )
        }, onFailure = { LoadResult.Error(it) })


    }
}