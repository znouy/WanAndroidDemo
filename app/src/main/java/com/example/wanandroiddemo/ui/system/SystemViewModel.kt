package com.example.wanandroiddemo.ui.system

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanandroiddemo.data.model.domain.SystemCategory
import com.example.wanandroiddemo.data.repository.SystemRepository
import com.example.wanandroiddemo.util.ext.coRunCatching
import com.example.wanandroiddemo.util.ext.logOnFailure
import com.example.wanandroiddemo.util.ext.mapNetworkException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// 定义密封接口（放在外层）
sealed interface SystemUiState {
    object Loading : SystemUiState
    data class Success(val categories: List<SystemCategory>) : SystemUiState
    data class Error(val message: String) : SystemUiState
}

@HiltViewModel
class SystemViewModel @Inject constructor(
    private val repository: SystemRepository
) : ViewModel() {

    // 1. 统一管理 UI 状态，初始状态为 Loading
    private val _uiState = MutableStateFlow<SystemUiState>(SystemUiState.Loading)

    // 2. 使用 asStateFlow()。这会在底层返回一个只读包装，彻底断绝外部强转修改的可能
    val uiState: StateFlow<SystemUiState> = _uiState.asStateFlow()

    init {
        fetchSystemCategories()
    }

    // 3. 暴露一个公共的重试方法，方便 Fragment 绑定错误页面的“点击重试”按钮
    fun retryFetch() {
        fetchSystemCategories()
    }

    private fun fetchSystemCategories() {
        viewModelScope.launch {
            // 开始请求前，先展示 Loading 状态
            _uiState.value = SystemUiState.Loading
            coRunCatching { repository.getSystemCategories() }
                .logOnFailure("获取分类失败")
                .mapNetworkException()
                .onSuccess {
                    _uiState.value = SystemUiState.Success(it)
                }
                .onFailure { _uiState.value = SystemUiState.Error(it.message ?: "未知错误") }
        }

    }


}