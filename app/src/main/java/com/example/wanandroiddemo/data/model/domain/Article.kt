package com.example.wanandroiddemo.data.model.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Article (
    val id: Int,
    val title: String,          // 清洗后的干净标题（去除 HTML 乱码）
    val desc: String,           // 描述文本（项目类文章有）
    val link: String,           // 网页链接
    val date: String,           //  清洗重组后的统一时间（如 "2024-03-01" 或 "刚刚"）
    val author: String,         //  自动合并作者/分享人后的名称
    val envelopePic: String,      // 统一的配图链接（项目封面图）
    val category: String,       //  自动拼接后的分类（如 "开发环境 / Android Studio"）
    val collect: Boolean,   // 统一的收藏状态

    //  UI 交互专用字段
    val isFavLoading: Boolean = false,
    val isTop: Boolean = false // 标记是否为置顶文章，默认关闭
    ): Parcelable
