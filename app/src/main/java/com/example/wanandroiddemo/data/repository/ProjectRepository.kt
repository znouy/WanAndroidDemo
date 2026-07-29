package com.example.wanandroiddemo.data.repository

import com.example.wanandroiddemo.data.api.ApiService
import com.example.wanandroiddemo.data.model.domain.ProjectCategory
import com.example.wanandroiddemo.data.model.dto.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 💡 项目（Project）分类数据仓库
 */
@Singleton
class ProjectRepository @Inject constructor(
    private val apiService: ApiService
) {
    /**
     * 获取项目分类列表
     *
     * 💡 详细解析这里的高阶函数和 Lambda 表达式：
     *
     * 1. `withContext(Dispatchers.IO) { ... }`
     *    - `withContext` 是协程库中的一个【高阶函数】。它的作用是切换协程的执行环境。
     *    - 接收参数：`Dispatchers.IO` (线程分配器，让任务在后台 I/O 线程池中排队执行) 和一个 Lambda 代码块。
     *    - 作用：将内部网络请求和数据解析的代码迁移到非 UI 线程执行。执行完成后，协程会自动恢复（Resume）到调用该方法的线程，并将最后一行结果返回。
     *
     * 2. `response.data.map { it.toDomain() }`
     *    - 这里的 `map` 是 Kotlin 标准库对 `Iterable`（可迭代集合）定义的【高阶函数】。
     *    - 它接收的参数是 `{ it.toDomain() }` 这个 Lambda 表达式。
     *    - 它的工作原理类似于一个 `for` 循环：
     *      ```kotlin
     *      val newList = mutableListOf<ProjectCategory>()
     *      for (item in response.data) {
     *          newList.add(item.toDomain())
     *      }
     *      return newList
     *      ```
     *    - 💡 什么是 `it`？
     *      Kotlin 中，如果 Lambda 只有一个参数，可以使用隐式参数 `it` 来代指该参数（在本例中 `it` 代表当前遍历到的 DTO 元素）。
     *
     * 3. `response.errorMsg.ifEmpty { "获取项目分类失败" }`
     *    - `ifEmpty` 是 Kotlin 扩展在 String/CharSequence 上的一个【高阶函数】。
     *    - 它接收一个无参 Lambda 返回字符串：`defaultValue: () -> C`。
     *    - 作用：如果当前字符串 `errorMsg` 长度为 0（即为空字符串 `""`），它就会调用这个 Lambda 并返回 Lambda 的结果作为后备默认值 `"获取项目分类失败"`；如果字符串不为空，则直接返回其自身值。
     */
    suspend fun getProjectCategories(): List<ProjectCategory> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        // 发起网络同步请求（挂起状态，非阻塞）
        val response = apiService.getProjectCategories()

        if (response.errorCode == 0) {
            val mapStartTime = System.currentTimeMillis()
            
            // 💡 高阶函数：将网络层实体转换成业务实体
            val result = response.data?.map { it.toDomain() }?:emptyList()

            // withContext 的最后一行表达式会被作为整个 withContext 的返回值
            result
        } else {
            // 💡 高阶函数：若服务器返回错误信息为空，则回退到默认文案并抛出异常
            throw Exception(response.errorMsg.ifEmpty { "获取项目分类失败" })
        }
    }
}
