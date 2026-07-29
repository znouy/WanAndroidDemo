package com.example.wanandroiddemo.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.wanandroiddemo.data.api.ApiService
import com.example.wanandroiddemo.data.model.dto.ArticleDto
import com.example.wanandroiddemo.util.ext.coRunCatching
import com.example.wanandroiddemo.util.ext.logOnFailure
import com.example.wanandroiddemo.util.ext.mapNetworkException

class WendaPagingSource(
    private val apiService: ApiService
) : PagingSource<Int, ArticleDto>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArticleDto> {
        // 问答接口第一页为 1，非 0
        val page = params.key ?: 1

        val result =
            coRunCatching { apiService.getWendaList(page) }
                .logOnFailure("getWendaList failure")
                .mapNetworkException()

        return result.fold(onSuccess = { response ->
            val data = response.data
            val dtoList = data?.datas?.filterNotNull() ?: emptyList()

            LoadResult.Page(
                data = dtoList, prevKey = if (page == 1) null else page - 1,
                // 如果当前加载的页码大于等于总页数，说明触底，没有下一页
                nextKey = if (page >= (data?.pageCount ?: 0)) null else page + 1
            )
        }, onFailure = { throwable ->
            LoadResult.Error(throwable)
        })
    }

    override fun getRefreshKey(state: PagingState<Int, ArticleDto>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}