package com.example.wanandroiddemo.data.repository

import com.example.wanandroiddemo.data.api.ApiService
import com.example.wanandroiddemo.data.model.domain.NavigationData
import com.example.wanandroiddemo.data.model.dto.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 💡 导航数据仓库类
 */
@Singleton
class NavigationRepository @Inject constructor(
    private val apiService: ApiService
) {
    /**
     * 获取导航分类数据列表
     * 
     * 💡 详细解析这里用到的高阶函数和 Lambda 表达式：
     * 
     * 1. `withContext(Dispatchers.IO) { ... }`
     *    - `withContext` 是协程库中的一个【高阶函数】，用于切换当前协程运行的线程上下文。
     *    - 它的参数之一是一个 Lambda 表达式（大括号里面的代码块）：`block: suspend CoroutineScope.() -> T`。
     *    - `Dispatchers.IO`：指定该代码块在专门做 I/O 操作（网络请求、数据库读写、文件读写）的线程池中执行。
     *    - 作用：切换到 I/O 线程请求网络，防止在主线程执行网络操作而导致主线程被阻塞（避免 ANR 无响应）。
     *    - 返回值：该高阶函数会返回 Lambda 块中最后一行的计算结果（这里要么是转换后的 List，要么是空列表）。
     * 
     * 2. `response.data.map { it.toDomain() }`
     *    - `map` 是 Kotlin 标准库中针对集合（List）扩展的一个非常重要的【高阶函数】。
     *    - 作用：遍历原集合中的每一个元素，并通过传入的转换函数将其变为另一种类型，最终生成并返回一个包含所有转换后元素的新集合。
     *    - 这里的参数是一个 Lambda 表达式：`{ it.toDomain() }`。
     *    - 💡 什么是 `it`？
     *      因为我们的 Lambda 只有一个参数，Kotlin 默认提供了一个隐式名称 `it`，无需写成 `element -> element.toDomain()`。
     *      这里的 `it` 代表原集合 `response.data` 中的某一个 DTO 项。
     *    - `it.toDomain()`：调用其对应的扩展转换函数，将 DTO 数据结构映射为 Domain 业务层数据结构。
     */
    suspend fun getNavigationData(): List<NavigationData> = withContext(Dispatchers.IO) {
        val response = apiService.getNavigationData()
        if (response.errorCode == 0) {
            // 将 DTO 列表逐个转换为业务层的 Domain 列表
            response.data?.map { it.toDomain() } ?: emptyList()
        } else {
            throw Exception(response.errorMsg.ifEmpty { "获取导航数据失败" })
        }
    }
}
