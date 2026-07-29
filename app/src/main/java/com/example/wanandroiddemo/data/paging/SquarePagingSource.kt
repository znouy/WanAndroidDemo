package com.example.wanandroiddemo.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.wanandroiddemo.data.api.ApiService
import com.example.wanandroiddemo.data.model.dto.ArticleDto
import com.example.wanandroiddemo.util.ext.coRunCatching
import com.example.wanandroiddemo.util.ext.logOnFailure
import com.example.wanandroiddemo.util.ext.mapNetworkException
import javax.inject.Inject

/**
 * 💡 广场分页数据源
 *
 * 泛型解释：
 * PagingSource<Key, Value>
 * - Key: 分页标识，这里使用 `Int` 代表页码。
 * - Value: 数据类型，代表广场的文章 DTO 对象 `ArticleDto`。
 */
class SquarePagingSource @Inject constructor(
    private val apiService: ApiService
) : PagingSource<Int, ArticleDto>() {

    /**
     * 加载分页数据
     * @param params 包含当前请求的 Key（页码）和 LoadSize（每页条数）
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArticleDto> {
        val page = params.key ?: 0

        val result = coRunCatching { apiService.getSquareArticles(page) }
            .logOnFailure("获取广场列表失败")
            .mapNetworkException()
        return result.fold(onSuccess = { response ->
            val data = response.data
            val cleanList: List<ArticleDto> = data?.datas?.filterNotNull() ?: emptyList()
            LoadResult.Page(
                data = cleanList,
                prevKey = if (page == 0) null else page - 1,
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
