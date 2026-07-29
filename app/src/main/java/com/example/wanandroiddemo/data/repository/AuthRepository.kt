package com.example.wanandroiddemo.data.repository

import com.example.wanandroiddemo.data.api.ApiService
import com.example.wanandroiddemo.data.local.AppPreferences
import com.example.wanandroiddemo.data.model.domain.User
import com.example.wanandroiddemo.data.model.dto.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService, private val appPreferences: AppPreferences
) {
   val userSessionFlow = appPreferences.userSessionFlow
    // 1. 执行登录
    suspend fun login(username: String, password: String): User = withContext(Dispatchers.IO) {
        val response = apiService.login(username, password)
        if (response.errorCode == 0 && response.data != null) {
            val user = response.data.toDomain()
            saveUserSession(user)
            user

        } else {
            throw Exception(response.errorMsg.ifEmpty { "登录失败" })
        }
    }

    // 2. 执行注册
    suspend fun register(username: String, password: String, repassword: String): User =
        withContext(Dispatchers.IO) {
            val response = apiService.register(username, password, repassword)
            if (response.errorCode == 0 && response.data != null) {
                val user = response.data.toDomain()
                saveUserSession(user)
                user
            } else {
                throw Exception(response.errorMsg.ifEmpty { "注册失败" })
            }
        }

    private suspend fun saveUserSession(user: User) {
        appPreferences.setUserSession(
            true, user.id, user.username, user.nickname
        )
    }
}