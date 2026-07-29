package com.example.wanandroiddemo.data.model.domain

data class ReadHistory(
    val id: Int,
    val title: String,
    val link: String,
    val readDate: String,
    val author: String = "",   // 文章有，网页无
    val category: String = "", // 文章有，网页无
    val niceDate: String = "", // 文章有，网页无
    val isArticle: Boolean = false // 💡 用于区分是标准文章还是自定义网页链接
)

fun ReadHistory.toArticle(): Article {
    //
    return Article(
        id = id,
        title = title,
        link = link,
        author = author,
        date = niceDate,
        category = category,
        isFavLoading = false,
        desc = "",
        envelopePic = "",
        collect = false,
    )
}