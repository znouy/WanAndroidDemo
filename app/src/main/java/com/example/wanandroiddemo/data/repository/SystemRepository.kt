package com.example.wanandroiddemo.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.wanandroiddemo.data.api.ApiService
import com.example.wanandroiddemo.data.model.domain.Article
import com.example.wanandroiddemo.data.model.domain.SystemCategory
import com.example.wanandroiddemo.data.model.dto.toDomain
import com.example.wanandroiddemo.data.paging.SystemArticlePagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 💡 知识体系（System）数据仓库类
 */
@Singleton
class SystemRepository @Inject constructor(
    private val apiService: ApiService
) {
    /**
     * 获取系统分类列表
     * @return 转换清洗后的业务模型列表
     *
     * 💡 详细解析这里的高阶函数和 Lambda 表达式：
     *
     * 1. `withContext(Dispatchers.IO) { ... }`
     *    - `withContext` 是协程库中的一个【高阶函数】，用来切换当前协程的执行线程。
     *    - 接收参数：`Dispatchers.IO` (告诉协程在后台 I/O 线程池中运行代码块) 和一个 Lambda 块。
     *    - 作用：将网络请求和数据解析移出主线程（UI 线程），防止导致界面卡顿，执行完毕后自动返回原线程并把最后一行结果作为返回值。
     *
     * 2. `response.data.map { it.toDomain() }`
     *    - `map` 是 Kotlin 集合库针对 `Iterable` 提供的一个【高阶扩展函数】。
     *    - 参数：Lambda 表达式 `{ it.toDomain() }`。
     *    - 作用：遍历 `response.data` 列表中的每个 DTO，应用 `toDomain()` 转换，生成并返回一个新的 `SystemCategory` 业务实体列表。
     *    - 💡 `it` 的概念：Lambda 只有一个参数时省略声明，默认隐式指代遍历到的那个元素。
     *
     * 3. `response.errorMsg.ifEmpty { "获取系统分类失败" }`
     *    - `ifEmpty` 是 String 类型的【高阶扩展函数】。
     *    - 作用：如果 `errorMsg` 是空字符串 `""`，则调用大括号中的 Lambda，返回 Lambda 的返回值 `"获取系统分类失败"`；否则直接返回原有的 `errorMsg`。
     */
    suspend fun getSystemCategories(): List<SystemCategory> = withContext(Dispatchers.IO) {
        // 1. 请求网络
        val response = apiService.getSystemCategories()

        // 2. 判断 WanAndroid 的接口状态
        if (response.errorCode == 0) {
            // 3. 状态成功，将 DTO 列表转换为 Domain 列表返回
            response.data?.map { it.toDomain() } ?: emptyList()
        } else {
            // 4. 状态失败，抛出带服务器错误信息的异常，ViewModel 中的 catch 块会捕获并展示给用户
            throw Exception(response.errorMsg.ifEmpty { "获取系统分类失败" })
        }
    }

    /**
     * 获取体系某个分类下的文章列表流，并对 DTO 进行转换
     *
     * 💡 详细解析这里的高阶函数、Lambda 表达式及闭包 (Closure)：
     *
     * 1. `pagingSourceFactory = { SystemArticlePagingSource(apiService, cid) }`
     *    - 这里的 `pagingSourceFactory` 接收的是一个函数类型的参数 `() -> PagingSource<Key, Value>`。
     *    - 这里的 `{ SystemArticlePagingSource(apiService, cid) }` 是传入它的【Lambda 表达式】。
     *    - 💡 闭包（Closure）的特性：
     *      这个 Lambda 表达式没有传入参数，但它“捕获”了外层方法的 `cid` 参数和构造函数中的 `apiService`。
     *      在 Kotlin 中，Lambda 内部可以无缝访问和使用它外部作用域中的局部变量和参数，这就是所谓的“闭包”。
     *      作用：每当 Paging 需要重新创建数据源时，这个 Lambda 都会带着外部最新的 `cid` 和 `apiService` 重新生成一个 `SystemArticlePagingSource`。
     *
     * 2. `.flow.map { pagingData -> ... }` 的解析：
     *    - 这里的 `map` 是 Flow（协程异步流）包下的【高阶函数】。
     *    - 其 Lambda 入参是 `pagingData` (类型是 `PagingData<ArticleDto>`)。
     *    - 作用：每当流向外界发射新的一页数据时，该高阶函数都会调用 Lambda 执行对整页数据的转换。
     *
     * 3. `pagingData.map { it.toDomain() }` 的解析：
     *    - 这里的 `map` 是 Paging 3 专门为 `PagingData` 增加的【高阶扩展函数】。
     *    - 作用：将当前页内包含的每一项 `ArticleDto`（通过隐式变量 `it` 引用）逐个调用其扩展函数 `toDomain()` 转换为本地业务实体 `Article`，从而在不破坏 Paging 分页特性的情况下实现底层实体和业务实体解耦。
     */
    fun getSystemArticles(cid: Int): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            // 💡 闭包 Lambda，捕获了外部作用域传递过来的 cid 变量
            pagingSourceFactory = { SystemArticlePagingSource(apiService, cid) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

}
