package com.example.wanandroiddemo.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wanandroiddemo.data.model.entity.ReadHistoryEntity

@Dao
interface ReadHistoryDao {

    //Room 原生完美支持返回 PagingSource，实现极简本地分页
    @Query("SELECT * FROM read_history ORDER BY readTimestamp DESC")
    fun getHistoryPagingSource(): PagingSource<Int, ReadHistoryEntity>

    // 插入历史（ID 重复则覆盖最新时间）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entity: ReadHistoryEntity)

    // 单个删除
    @Query("DELETE FROM read_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)

    // 清空历史
    @Query("DELETE FROM read_history")
    suspend fun clearAllHistory()
}