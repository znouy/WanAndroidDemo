package com.example.wanandroiddemo.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.wanandroiddemo.data.api.ApiService
import com.example.wanandroiddemo.data.model.dto.CollectArticleDto
import com.example.wanandroiddemo.util.ext.coRunCatching
import com.example.wanandroiddemo.util.ext.logOnFailure
import com.example.wanandroiddemo.util.ext.mapNetworkException

class CollectPagingSource(
    private val apiService: ApiService
) : PagingSource<Int, CollectArticleDto>() {

    override fun getRefreshKey(state: PagingState<Int, CollectArticleDto>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CollectArticleDto> {
        val page = params.key ?: 0 // WanAndroid 收藏接口从第 0 页开始
        val result = coRunCatching { apiService.getCollectList(page) }
            .logOnFailure("获取收藏列表失败")
            .mapNetworkException()
        return result.fold(onSuccess = {
            val listData = it.data?.datas ?: emptyList()
            LoadResult.Page(
                data = listData,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (listData.isEmpty()) null else page + 1
            )
        }, onFailure = {
            LoadResult.Error(it)
        })
    }

}