package com.example.wanandroiddemo.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanandroiddemo.data.model.domain.User
import com.example.wanandroiddemo.data.repository.AuthRepository
import com.example.wanandroiddemo.util.ext.coRunCatching
import com.example.wanandroiddemo.util.ext.logOnFailure
import com.example.wanandroiddemo.util.ext.mapNetworkException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthUiState {
    object Idle : AuthUiState                       // 空闲/初始状态
    object Loading : AuthUiState                    // 登录中/注册中...
    data class Success(val user: User) : AuthUiState // 成功
    data class Error(val message: String) : AuthUiState // 失败
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()

    // 1. 登录
    fun login(username: String, password: String) {
        if (username.isEmpty() || password.isEmpty()) {
            _uiState.value = AuthUiState.Error("用户名或密码不能为空")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            coRunCatching { repository.login(username, password) }
                .logOnFailure("登录失败")
                .mapNetworkException()
                .onSuccess {
                    _uiState.value = AuthUiState.Success(it)
                }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "登录出错") }
        }
    }

    // 2. 注册
    fun register(username: String, password: String, repassword: String) {
        if (username.isEmpty() || password.isEmpty() || repassword.isEmpty()) {
            _uiState.value = AuthUiState.Error("输入框不能为空")
            return
        }
        if (password != repassword) {
            _uiState.value = AuthUiState.Error("两次输入的密码不一致")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val user = repository.register(username, password, repassword)
                _uiState.value = AuthUiState.Success(user)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "注册出错")
            }
        }
    }
}

