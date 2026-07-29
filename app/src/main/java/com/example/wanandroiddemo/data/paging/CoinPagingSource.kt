package com.example.wanandroiddemo.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.wanandroiddemo.data.api.ApiService
import com.example.wanandroiddemo.data.model.dto.CoinRecordDto
import com.example.wanandroiddemo.util.ext.coRunCatching
import com.example.wanandroiddemo.util.ext.logOnFailure
import com.example.wanandroiddemo.util.ext.mapNetworkException

class CoinPagingSource(
    private val apiService: ApiService
) : PagingSource<Int, CoinRecordDto>() {

    override fun getRefreshKey(state: PagingState<Int, CoinRecordDto>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CoinRecordDto> {
        val page = params.key ?: 1 // WanAndroid 积分接口从第一页开始
        val result = coRunCatching {
            apiService.getCoinHistoryList(page)
        }.logOnFailure("获取积分列表失败")
            .mapNetworkException()

        return result.fold(onSuccess = { response ->
            val listData = response.data?.datas ?: emptyList()
            LoadResult.Page(
                data = listData,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (listData.isEmpty()) null else page + 1
            )
        }, onFailure = { throwable ->
            LoadResult.Error(throwable)
        })
    }
}