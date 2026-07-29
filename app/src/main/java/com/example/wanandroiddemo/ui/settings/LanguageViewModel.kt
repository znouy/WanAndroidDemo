package com.example.wanandroiddemo.ui.settings

import androidx.lifecycle.ViewModel
import com.example.wanandroiddemo.data.repository.LanguageItem
import com.example.wanandroiddemo.data.repository.LanguageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val repository: LanguageRepository
) : ViewModel() {

    private val _languageList = MutableStateFlow<List<LanguageItem>>(emptyList())
    val languageList: StateFlow<List<LanguageItem>> = _languageList.asStateFlow()

    init {
        loadLanguages()
    }

    /**
     * 加载语言列表并标记当前选中的语言
     */
    fun loadLanguages() {
        _languageList.value = repository.getSupportedLanguages()
    }

    /**
     * 切换应用语言
     */
    fun toggleLanguage(languageItem: LanguageItem) {
        repository.changeLanguages(languageItem)
    }
}

