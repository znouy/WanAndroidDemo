package com.example.wanandroiddemo.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanandroiddemo.data.model.domain.NavigationData
import com.example.wanandroiddemo.data.repository.NavigationRepository
import com.example.wanandroiddemo.util.ext.coRunCatching
import com.example.wanandroiddemo.util.ext.logOnFailure
import com.example.wanandroiddemo.util.ext.mapNetworkException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface NavigationUiState {
    object Loading : NavigationUiState
    data class Success(val categories: List<NavigationData>) :NavigationUiState
    data class Error(val message: String) : NavigationUiState
}
@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val repository: NavigationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<NavigationUiState>(NavigationUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        fetchNavigation()
    }

    fun reFetch(){
        fetchNavigation()
    }
    private fun fetchNavigation() {
        viewModelScope.launch {
            _uiState.value = NavigationUiState.Loading
            coRunCatching { repository.getNavigationData() }
                .logOnFailure("获取导航数据失败")
                .mapNetworkException()
                .onSuccess { _uiState.value = NavigationUiState.Success(it) }
                .onFailure { _uiState.value = NavigationUiState.Error(it.message?:"未知错误") }
        }
    }
}
