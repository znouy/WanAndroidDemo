package com.example.wanandroiddemo.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.wanandroiddemo.data.api.ApiService
import com.example.wanandroiddemo.data.model.domain.Article
import com.example.wanandroiddemo.data.model.dto.toDomain
import com.example.wanandroiddemo.data.paging.SquarePagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 💡 广场数据仓库类
 */
@Singleton
class SquareRepository @Inject constructor(
    private val apiService: ApiService
) {
    /**
     * 获取广场文章列表流（Flow）
     * 返回类型为 `Flow<PagingData<Article>>`，表示一个可持续发射分页数据的冷数据流。
     *
     * 💡 详细解析这里的高阶函数和 Lambda 表达式：
     *
     * 1. 什么是高阶函数 (Higher-Order Function)？
     *    - 函数如果可以接收另一个函数作为参数，或者它的返回值是一个函数，则它就被称为高阶函数。
     *
     * 2. `pagingSourceFactory = { SquarePagingSource(apiService) }`
     *    - `Pager` 类的构造器接收一个命名为 `pagingSourceFactory` 的参数，它的类型是函数类型 `() -> PagingSource<Key, Value>`。
     *    - 这里的 `{ SquarePagingSource(apiService) }` 就是传递给它的【Lambda 表达式】（匿名函数）。
     *    - 它没有入参，返回一个新的 `SquarePagingSource` 实例。
     *    - 作用：当 Paging 3 内部由于数据失效、刷新（Refresh）等原因需要获取全新数据源时，会调用这个函数来创建一个新的实例。
     *
     * 3. `flow` 属性：
     *    - 调用 `Pager` 的 `.flow` 属性，能够将 Paging 的内部状态转化为一个 `Flow<PagingData<ArticleDto>>`。
     *
     * 4. `.flow.map { pagingData -> ... }` 的解析：
     *    - 这里的 `map` 是协程冷流（Flow）中的一个【高阶函数】，用于数据在流中的转换加工。
     *    - 它接收的参数是一个 Lambda 表达式 `{ pagingData -> ... }`。
     *    - `pagingData` 作为 Lambda 的入参，代表每次 Flow 发射出来的包含 `ArticleDto` 原始网络实体的数据块对象。
     *
     * 5. `pagingData.map { it.toDomain() }` 的解析：
     *    - 这里的第二个 `map` 则是 Paging 3 库专门为 `PagingData` 类型提供的一个【高阶扩展函数】。
     *    - 作用：用来对其内部打包的每一个元素（本例中为 `ArticleDto`）执行转换映射，并输出一个全新的、装载着业务对象 `Article` 的 `PagingData<Article>`。
     *    - 接收的参数是 Lambda `{ it.toDomain() }`。
     *    - 💡 什么是 `it`？
     *      当 Lambda 表达式只有一个输入参数时，Kotlin 允许隐式指定其参数名为 `it`，在这里 `it` 代表当前需要转换的 `ArticleDto` 网络实体。
     *      通过调用 `it.toDomain()`，将其转换成业务层的 `Article` 数据实体。
     */
    fun getSquareArticles(): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),// TODO:  enablePlaceholders
            pagingSourceFactory = { SquarePagingSource(apiService) }
        ).flow.map { pagingData -> 
            pagingData.map { it.toDomain() } 
        }
    }
}
