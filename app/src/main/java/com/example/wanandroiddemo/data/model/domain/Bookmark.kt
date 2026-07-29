package com.example.wanandroiddemo.data.model.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 干净的书签业务模型，所有字段全空安全
 */
@Parcelize
data class Bookmark(
    val id: Int,
    val name: String,
    val link: String
) : Parcelable

// =========================================================================
// ：将 Bookmark 适配/转换为临时的 Article 模型，实现 Web 详情页的 复用
// =========================================================================
fun Bookmark.toArticle(): Article {
    //
    return Article(
        id = this.id,
        title = this.name,               // 书签名字映射为文章标题
        link = this.link,                 // 书签链接映射为文章链接
        author = "",
        date = "",
        category = "书签",
        isFavLoading = false,
        desc = "",
        envelopePic = "",
        collect = false,
    )
}