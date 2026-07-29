package com.example.wanandroiddemo.data.model.domain

data class ProjectCategory(
    val articleList: List<Article>,
    val author: String,
    val children: List<ProjectCategory>,
    val courseId: Int,
    val cover: String,
    val desc: String,
    val id: Int,
    val lisense: String,
    val lisenseLink: String,
    val name: String,
    val order: Int,
    val parentChapterId: Int,
    val type: Int,
    val userControlSetTop: Boolean,
    val visible: Int
)