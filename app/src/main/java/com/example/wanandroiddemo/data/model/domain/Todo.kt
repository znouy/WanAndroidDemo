package com.example.wanandroiddemo.data.model.domain

import android.os.Parcelable
import com.example.wanandroiddemo.data.model.dto.TodoPriority
import kotlinx.parcelize.Parcelize

/**
 * 干净的 TODO 业务模型
 */
@Parcelize
data class Todo(
    val id: Int,
    val title: String,
    val content: String,
    val dateStr: String,
    val isDone: Boolean,
    val priority: TodoPriority
): Parcelable