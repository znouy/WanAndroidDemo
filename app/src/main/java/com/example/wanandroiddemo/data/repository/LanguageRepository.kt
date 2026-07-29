package com.example.wanandroiddemo.data.repository

import com.example.wanandroiddemo.R
import com.example.wanandroiddemo.data.local.LocaleManager
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageRepository @Inject constructor(
    private val localeManager: LocaleManager
) {
    /**
     * 获取系统支持的语言列表
     */
    fun getSupportedLanguages(): List<LanguageItem> {
        // 获取当前激活的应用语言偏好
        val languageTag = localeManager.getCurrentLanguageTag()
        Timber.d("languageTag:$languageTag")

        //动态获取手机系统当前的语言名称（例如系统是中文就显示"中文"，是英文就显示"English"）
        val systemLanguageName = localeManager.getSystemDefaultLanguageName()


        // 3. 构造列表，主标题全部使用 R.string.xxx
        return listOf(
            LanguageItem(
                "", R.string.language_system, systemLanguageName, languageTag.isEmpty()
            ),
            LanguageItem(
                "en", R.string.language_english, "English", languageTag.startsWith("en")
            ),
            LanguageItem("zh", R.string.language_chinese, "中文", languageTag.startsWith("zh")),
            LanguageItem("ar", R.string.language_arabic, "العربية", languageTag.startsWith("ar")),
            LanguageItem(
                "es", R.string.language_spanish, "español", languageTag.startsWith("es")
            ),
            LanguageItem(
                "fr", R.string.language_french, "français", languageTag.startsWith("fr")
            ),
            LanguageItem("ru", R.string.language_russian, "русский", languageTag.startsWith("ru"))
        )
    }


    /**
     * 💡 移到这里：获取当前语言的资源 ID（业务映射规则）
     */
    fun getCurrentLanguageNameRes(): Int {
        val tag = localeManager.getCurrentLanguageTag()
        return when {
            tag.isEmpty() -> R.string.language_system
            tag.startsWith("en") -> R.string.language_english
            tag.startsWith("zh") -> R.string.language_chinese
            tag.startsWith("ar") -> R.string.language_arabic
            tag.startsWith("es") -> R.string.language_spanish
            tag.startsWith("fr") -> R.string.language_french
            tag.startsWith("ru") -> R.string.language_russian
            else -> R.string.language_system
        }
    }

    /**
     * 修改系统语言
     * */
    fun changeLanguages(languageItem: LanguageItem) {
        localeManager.setAppLanguage(languageItem.languageTag)
    }
}

data class LanguageItem(
    val languageTag: String, // 语言代码，例如 "en", "zh", "system" 代表跟随系统
    val displayName: Int,  // 对应当前系统语言下的名称，如 "英语"
    val nativeName: String,   // 该语言的本名，如 "English"
    val isSelected: Boolean   // 是否被选中
)