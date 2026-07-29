package com.example.wanandroiddemo.data.paging
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.wanandroiddemo.data.api.ApiService
import com.example.wanandroiddemo.data.model.domain.Article
import com.example.wanandroiddemo.data.model.dto.toDomain

class SearchPagingSource(
    private val apiService: ApiService,
    private val query: String
) : PagingSource<Int, Article>() {

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        // WanAndroid 搜索接口页码从 0 开始
        val page = params.key ?: 0
        return try {
            val response = apiService.searchArticles(page, query)
            if (response.errorCode == 0 && response.data != null) {
                val dtoList = response.data.datas ?: emptyList()
                val domainList = dtoList.mapNotNull { it?.toDomain() }// 数据静默清洗

                LoadResult.Page(
                    data = domainList,
                    prevKey = if (page == 0) null else page - 1,
                    // 如果返回的数据为空，说明到底了，把 nextKey 设为 null 停止分页
                    nextKey = if (domainList.isEmpty()) null else page + 1
                )
            } else {
                LoadResult.Error(Exception(response.errorMsg.ifEmpty { "搜索失败" }))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}