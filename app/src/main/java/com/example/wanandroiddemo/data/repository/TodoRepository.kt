package com.example.wanandroiddemo.data.repository

import com.example.wanandroiddemo.data.api.ApiService
import com.example.wanandroiddemo.data.model.dto.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TodoRepository @Inject constructor(
    private val apiService: ApiService
) {
    // 获取 任务列表
    suspend fun getTodoList(page: Int, status: Int?) = withContext(Dispatchers.IO) {
        val response = apiService.getTodoList(page, status)
        if (response.errorCode == 0) {
            response.data?.datas?.map { it.toDomain() } ?: emptyList()
        } else {
            throw Exception(response.errorMsg.ifEmpty { "获取任务列表失败" })
        }
    }

    //新增任务
    suspend fun addTodo(title: String, content: String, date: String, priority: Int) =
        withContext(Dispatchers.IO) {
            val response = apiService.addTodo(title, content, date, priority)
            if (response.errorCode != 0) {
                throw Exception(response.errorMsg.ifEmpty { "创建任务失败" })
            }
        }

    //删除任务
    suspend fun deleteTodo(id: Int) = withContext(Dispatchers.IO) {
        val response = apiService.deleteTodo(id)
        if (response.errorCode != 0) {
            throw Exception(response.errorMsg.ifEmpty { "删除任务失败" })
        }
    }

    // 更新一个任务
    suspend fun updateTodo(
        id: Int,
        title: String,
        content: String,
        date: String,
        isDone: Boolean,
        priority: Int
    ) = withContext(Dispatchers.IO) {
        val status = if (isDone) 1 else 0
        val response = apiService.updateTodo(id, title, content, date, status, priority)
        if (response.errorCode != 0) {
            throw Exception(response.errorMsg.ifEmpty { "更新任务失败" })
        }
    }

    // 切换完成状态
    suspend fun toggleStatus(id: Int, isDone: Boolean) = withContext(Dispatchers.IO) {
        val status = if (isDone) 1 else 0
        val response = apiService.toggleTodoStatus(id, status)
        if (response.errorCode != 0) {
            throw Exception(response.errorMsg.ifEmpty { "切换任务状态失败" })
        }
    }
}