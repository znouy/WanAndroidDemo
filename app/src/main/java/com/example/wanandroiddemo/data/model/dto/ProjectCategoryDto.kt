package com.example.wanandroiddemo.data.model.dto

import com.example.wanandroiddemo.data.model.domain.ProjectCategory
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProjectCategoryDto(
    @Json(name = "articleList")
    val articleList: List<ArticleDto?>?,
    @Json(name = "author")
    val author: String?,
    @Json(name = "children")
    val children: List<ProjectCategoryDto?>?,
    @Json(name = "courseId")
    val courseId: Int?,
    @Json(name = "cover")
    val cover: String?,
    @Json(name = "desc")
    val desc: String?,
    @Json(name = "id")
    val id: Int?,
    @Json(name = "lisense")
    val lisense: String?,
    @Json(name = "lisenseLink")
    val lisenseLink: String?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "order")
    val order: Int?,
    @Json(name = "parentChapterId")
    val parentChapterId: Int?,
    @Json(name = "type")
    val type: Int?,
    @Json(name = "userControlSetTop")
    val userControlSetTop: Boolean?,
    @Json(name = "visible")
    val visible: Int?
)

fun ProjectCategoryDto.toDomain(): ProjectCategory {
    return ProjectCategory(
        articleList = articleList?.mapNotNull { it?.toDomain() }.orEmpty(),
        author = author.orEmpty(),
        children = children?.mapNotNull { it?.toDomain() }.orEmpty(),
        courseId = courseId ?: 0,
        cover = cover.orEmpty(),
        desc = desc.orEmpty(),
        id = id ?: 0,
        lisense = lisense.orEmpty(),
        lisenseLink = lisenseLink.orEmpty(),
        name = name.orEmpty(),
        order = order ?: 0,
        parentChapterId = parentChapterId ?: 0,
        type = type ?: 0,
        userControlSetTop = userControlSetTop ?: false,
        visible = visible ?: 0
    )
}



