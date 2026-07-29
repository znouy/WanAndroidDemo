package com.example.wanandroiddemo.data.model.dto

import com.example.wanandroiddemo.data.model.domain.Article
import com.example.wanandroiddemo.util.ext.htmlDecode
import com.example.wanandroiddemo.util.ext.toCharBreak
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CollectArticleDto(
    @Json(name = "id") val id: Int?,               // 收藏记录 ID
    @Json(name = "originId") val originId: Int?,     // 原始文章 ID (取消收藏时使用)
    @Json(name = "title") val title: String?,
    @Json(name = "link") val link: String?,
    @Json(name = "author") val author: String?,
    @Json(name = "niceDate") val niceDate: String?,
    @Json(name = "envelopePic") val envelopePic: String?, // 缩略图
    @Json(name = "chapterName") val chapterName: String?  // 分类名称
)


/**
 * 将收藏 DTO 转换为通用的 Domain Article 实体
 * 这样就可以直接复用首页的列表布局和 Adapter 了！
 */
fun CollectArticleDto.toDomain(): Article {

    // 在我的收藏列表中，取消收藏接口（uncollect_originId）必须使用文章原站点的原始 ID（originId）。
    // 如果没有 originId（比如用户手动添加的自定义 H5 链接收藏），才降级退回到收藏记录的 id。
    val articleId = if (originId != null && originId != -1) {
        originId
    } else {
        id ?: 0
    }

    return Article(
        id = articleId,
        title = title.htmlDecode().ifEmpty { "未命名文章" }.toCharBreak(),
        desc = "",
        link = link.orEmpty(),
        date = niceDate.orEmpty().ifEmpty  { "未知时间" },
        author = author.orEmpty().ifEmpty { "匿名用户" },
        envelopePic = envelopePic.orEmpty(),
        category = chapterName.orEmpty(),
        collect = true,
        isFavLoading = false
    )
}