package com.example.wanandroiddemo.data.repository

import com.example.wanandroiddemo.data.api.ApiService
import com.example.wanandroiddemo.data.model.domain.Bookmark
import com.example.wanandroiddemo.data.model.dto.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepository @Inject constructor(
    private val apiService: ApiService
) {
    // 获取书签列表（IO 挂起执行）
    suspend fun getBookmarks(): List<Bookmark> = withContext(Dispatchers.IO) {
        val response = apiService.getBookmarkList()
        if (response.errorCode == 0) {
            response.data?.map { it.toDomain() }?.sortedByDescending { it.id } ?: emptyList()
        } else {
            throw Exception(response.errorMsg.ifEmpty { "获取书签列表失败" })
        }
    }

    // 添加书签
    suspend fun addBookmark(name: String, link: String) = withContext(Dispatchers.IO) {
        val response = apiService.addBookmark(name, link)
        if (response.errorCode != 0) {
            throw Exception(response.errorMsg.ifEmpty { "添加书签失败" })
        }
    }

    // 更新书签
    suspend fun updateBookmark(id: Int, name: String, link: String) = withContext(Dispatchers.IO) {
        val response = apiService.updateBookmark(id, name, link)
        if (response.errorCode != 0) {
            throw Exception(response.errorMsg.ifEmpty { "更新书签失败" })
        }
    }

    // 删除书签
    suspend fun deleteBookmark(id: Int) = withContext(Dispatchers.IO) {
        val response = apiService.deleteBookmark(id)
        if (response.errorCode != 0) {
            throw Exception(response.errorMsg.ifEmpty { "删除书签失败" })
        }
    }
}