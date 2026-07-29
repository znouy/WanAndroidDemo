package com.example.wanandroiddemo.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.wanandroiddemo.data.api.ApiService
import com.example.wanandroiddemo.data.model.dto.ArticleDto
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

/**
 * 💡 体系文章分页数据源
 *
 * 💡 什么是 辅助注入 (Assisted Injection)？
 * 在 Hilt 依赖注入中，通常所有的依赖都是在编译期静态确定的（比如 ApiService 始终由 Hilt 自动创建并注入）。
 * 但有时我们需要在创建对象时传入一些【运行时才知道的动态参数】。比如这里的 `cid`（分类 ID），每个分类页面对应的 `cid` 都不同。
 *
 * 这时候我们就需要使用 Assisted Injection：
 * 1. 在构造函数上使用 `@AssistedInject` 替代原来的 `@Inject`。
 * 2. 对于那些需要动态传递的变量，在其前面加上 `@Assisted` 注解。
 * 3. 定义一个内部接口，加上 `@AssistedFactory` 注解，声明一个创建方法。Hilt 会自动生成该工厂接口的实现。
 */
class SystemArticlePagingSource @AssistedInject constructor(
    private val apiService: ApiService,
    @Assisted private val cid: Int // 💡 运行时动态传递的分类 ID (cid)
) : PagingSource<Int, ArticleDto>() {

    /**
     * 💡 辅助注入工厂接口
     * Hilt 会在后台自动为这个 Factory 编写实现代码。
     * 当我们在 Repository 想要创建 SystemArticlePagingSource 时，只需要注入这个 `Factory`，然后调用 `create(cid)` 即可。
     */
    @AssistedFactory
    interface Factory {
        fun create(cid: Int): SystemArticlePagingSource
    }

    /**
     * 分页数据加载核心方法
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArticleDto> {
        val page = params.key ?: 0
        
        return try {
            // 调用 ApiService 发起网络请求。
            // 传入当前请求页码 `page` 和当前分类的标识 `cid`。
            val response = apiService.getSystemArticles(page, cid)
            val data = response.data
            val cleanArticles: List<ArticleDto> = data?.datas?.filterNotNull() ?: emptyList()
            LoadResult.Page(
                data = cleanArticles,
                // WanAndroid 首页文章是从 0 开始的，但体系文章有时是从 0 或 1 开始，需根据实际接口调试
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (page < (data?.pageCount ?: 0)) page + 1 else null
            )
        } catch (e: Exception) {
            // 如果遇到异常，返回封装的 LoadResult.Error，以供 Paging 处理错误状态
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ArticleDto>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
