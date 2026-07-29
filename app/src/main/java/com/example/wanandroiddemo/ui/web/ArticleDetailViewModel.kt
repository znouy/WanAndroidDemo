package com.example.wanandroiddemo.ui.web

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanandroiddemo.data.model.domain.Article
import com.example.wanandroiddemo.data.repository.HistoryRepository
import com.example.wanandroiddemo.util.ext.coRunCatching
import com.example.wanandroiddemo.util.ext.logOnFailure
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    fun recordHistory(article: Article) {

        viewModelScope.launch {
            coRunCatching { historyRepository.recordHistory(article) }
                .logOnFailure("插入阅读历史记录失败")
                .onSuccess { Timber.d("插入记录成功：id=${article.id}") }
        }
    }
}