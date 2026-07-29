package com.example.wanandroiddemo.ui.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanandroiddemo.data.model.domain.ProjectCategory
import com.example.wanandroiddemo.data.repository.ProjectRepository
import com.example.wanandroiddemo.util.ext.coRunCatching
import com.example.wanandroiddemo.util.ext.logOnFailure
import com.example.wanandroiddemo.util.ext.mapNetworkException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProjectUiState {
    object Loading : ProjectUiState
    data class Success(val data: List<ProjectCategory>) : ProjectUiState
    data class Error(val message: String) : ProjectUiState
}

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val repository: ProjectRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProjectUiState>(ProjectUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        fetchProjectCategories()
    }
    fun reFetch(){
        fetchProjectCategories()
    }

    private fun fetchProjectCategories() {
        viewModelScope.launch {
            _uiState.value = ProjectUiState.Loading
            coRunCatching { repository.getProjectCategories() }
                .logOnFailure("获取项目分类失败")
                .mapNetworkException()
                .onSuccess { _uiState.value = ProjectUiState.Success(it) }
                .onFailure { _uiState.value = ProjectUiState.Error(it.message ?: "未知错误") }

        }
    }


}