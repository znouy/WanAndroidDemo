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
 * 💡 什么是 PagingSource？
 * PagingSource 是 Paging 3 库的核心组件之一，负责从指定的数据源（如网络 API、本地数据库）加载分页数据。
 *
 * 泛型解释：
 * PagingSource<Key, Value>
 * - Key: 分页请求的标识类型。这里是 `Int`，表示用页码（0, 1, 2...）来请求数据。
 * - Value: 每一项数据的类型。这里是 `ArticleDto`，表示返回的文章网络数据传输对象。
 */
class ArticlePagingSource @Inject constructor(
    private val apiService: ApiService
) : PagingSource<Int, ArticleDto>() {


    /*override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArticleDto> {
        // 1. 获取当前需要加载的页码。
        // params.key 就是下一次分页请求的页码。如果是第一次加载，params.key 会是 null。
        // 💡 什么是 `?:`（Elvis 运算符/空合并运算符）？
        // 如果 `params.key` 不为 null，则使用它的值；如果是 null，则返回右侧的默认值 `0`（即默认从第 0 页开始请求）。
        val page = params.key ?: 0

        return try {
            // 发起挂起网络请求，获取第 page 页的文章数据
            val response = apiService.getArticles(page)
            val data = response.data
            
            val cleanArticles: List<ArticleDto> = data?.datas?.filterNotNull() ?: emptyList()
            
            // 加载成功，构建 Page 对象返回
            LoadResult.Page(
                data = cleanArticles, // 传入清洗后的绝对安全列表
                // 如果当前已经是第 0 页，说明没有上一页了，prevKey 设为 null；否则上一页是 page - 1
                prevKey = if (page == 0) null else page - 1,
                // 安全获取总页数并判断：如果当前页码大于等于总页码(pageCount)，说明已经没有下一页了，nextKey 设为 null；否则下一页是 page + 1
                // 💡 `data.pageCount ?: 0`：利用 Elvis 运算符处理 pageCount 为 null 的情况，若为 null 则当做 0 处理。
                nextKey = if (page >= (data?.pageCount ?: 0)) null else page + 1
            )
        } catch (e: Exception) {
            // 如果捕获到网络异常、解析异常等，返回 LoadResult.Error，Paging 会在 UI 上触发加载失败状态
            LoadResult.Error(e)
        }
    }*/

    override fun getRefreshKey(state: PagingState<Int, ArticleDto>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArticleDto> {
        val page = params.key ?: 0


        val result =
            coRunCatching { apiService.getArticles(page) }
                .logOnFailure("获取首页文章分页失败")
                .mapNetworkException()

        //利用 Result 的折叠（fold）函数，无缝映射为 Paging 3 规范的 LoadResult
        return result.fold(onSuccess = { response ->
            val data = response.data
            val cleanArticles: List<ArticleDto> = data?.datas?.filterNotNull() ?: emptyList()
            LoadResult.Page(
                data = cleanArticles,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (page+1 >= (data?.pageCount ?: 0)) null else page + 1
            )
        }, onFailure = { throwable ->
            // 此时抛出的 throwable 已经是被 mapNetworkException() 净化翻译过后的异常
            // 它携带的 message 会直接呈现在 UI 的错误状态占位图上
            LoadResult.Error(throwable)
        })
    }
}
