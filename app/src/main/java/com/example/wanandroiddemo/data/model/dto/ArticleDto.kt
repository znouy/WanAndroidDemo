package com.example.wanandroiddemo.data.model.dto

import com.example.wanandroiddemo.data.model.domain.Article
import com.example.wanandroiddemo.util.ext.htmlDecode
import com.example.wanandroiddemo.util.ext.toCharBreak
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ArticleDto(
    @Json(name = "adminAdd")
    val adminAdd: Boolean?,
    @Json(name = "apkLink")
    val apkLink: String?,
    @Json(name = "audit")
    val audit: Int?,
    @Json(name = "author")
    val author: String?,
    @Json(name = "canEdit")
    val canEdit: Boolean?,
    @Json(name = "chapterId")
    val chapterId: Int?,
    @Json(name = "chapterName")
    val chapterName: String?,
    @Json(name = "collect")
    val collect: Boolean?,
    @Json(name = "courseId")
    val courseId: Int?,
    @Json(name = "desc")
    val desc: String?,
    @Json(name = "descMd")
    val descMd: String?,
    @Json(name = "envelopePic")
    val envelopePic: String?,
    @Json(name = "fresh")
    val fresh: Boolean?,
    @Json(name = "host")
    val host: String?,
    @Json(name = "id")
    val id: Int?,
    @Json(name = "isAdminAdd")
    val isAdminAdd: Boolean?,
    @Json(name = "link")
    val link: String?,
    @Json(name = "niceDate")
    val niceDate: String?,
    @Json(name = "niceShareDate")
    val niceShareDate: String?,
    @Json(name = "origin")
    val origin: String?,
    @Json(name = "prefix")
    val prefix: String?,
    @Json(name = "projectLink")
    val projectLink: String?,
    @Json(name = "publishTime")
    val publishTime: Long?,
    @Json(name = "realSuperChapterId")
    val realSuperChapterId: Int?,
    @Json(name = "selfVisible")
    val selfVisible: Int?,
    @Json(name = "shareDate")
    val shareDate: Long?,
    @Json(name = "shareUser")
    val shareUser: String?,
    @Json(name = "superChapterId")
    val superChapterId: Int?,
    @Json(name = "superChapterName")
    val superChapterName: String?,
    @Json(name = "tags")
    val tags: List<Tag?>?,
    @Json(name = "title")
    val title: String?,
    @Json(name = "type")
    val type: Int?,
    @Json(name = "userId")
    val userId: Int?,
    @Json(name = "visible")
    val visible: Int?,
    @Json(name = "zan")
    val zan: Int?
)

@JsonClass(generateAdapter = true)
data class Tag(
    @Json(name = "name")
    val name: String?,
    @Json(name = "url")
    val url: String?
)

fun ArticleDto.toDomain(): Article {
    //  拼接一级/二级分类（例如：“开发环境 / 样式与主题”）
    val cleanCategory = when {
        !superChapterName.isNullOrEmpty() && !chapterName.isNullOrEmpty() -> {
            "$superChapterName /$chapterName"
        }

        !chapterName.isNullOrEmpty() -> chapterName
        else -> "通用分类"
    }

    val cleanLink = when {
        link.isNullOrEmpty() -> ""

        else -> link
    }


    return Article(
        id = id ?: 0,
        title = title.htmlDecode().ifEmpty { "未命名文章" }.toCharBreak(),
        desc = desc.orEmpty(),
        link = cleanLink,
        date = niceDate.orEmpty().ifEmpty { niceShareDate.orEmpty() }.ifEmpty { "未知时间" },
        author = author.orEmpty().ifEmpty { shareUser.orEmpty() }.ifEmpty { "微信分享" },
        envelopePic = envelopePic.orEmpty(),
        category = cleanCategory,
        collect = collect ?: false,
        isFavLoading = false
    )
}

