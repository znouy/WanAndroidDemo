package com.example.wanandroiddemo.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.wanandroiddemo.data.database.dao.ArticleDao
import com.example.wanandroiddemo.data.database.dao.ReadHistoryDao
import com.example.wanandroiddemo.data.model.entity.ArticleEntity
import com.example.wanandroiddemo.data.model.entity.ReadHistoryEntity

@Database(
    entities = [ArticleEntity::class, ReadHistoryEntity::class],
    version = 1,                       // 数据库版本号
    exportSchema = false  //告诉 Room 在编译时不将数据库的结构（Schema）导出为 JSON 文件
)
abstract class WanAndroidDatabase : RoomDatabase() {
    // 声明获取 DAO 的抽象方法
    abstract fun articleDao(): ArticleDao
    abstract fun readHistoryDao(): ReadHistoryDao

}