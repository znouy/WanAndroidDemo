package com.example.wanandroiddemo.di

import android.content.Context
import androidx.room.Room
import com.example.wanandroiddemo.data.database.WanAndroidDatabase
import com.example.wanandroiddemo.data.database.dao.ArticleDao
import com.example.wanandroiddemo.data.database.dao.ReadHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context // Hilt 会自动提供 Application 级别的 Context
    ): WanAndroidDatabase {
        return Room.databaseBuilder(
            context,
            WanAndroidDatabase::class.java,
            "wan_android_database" // 本地数据库文件名
        )
            // 实际开发中，如果版本升级需要配置 Migration
            // .fallbackToDestructiveMigration() // 测试阶段可以使用这个，版本升级时会清空旧数据避免崩溃
            .build()
    }

    @Provides
    @Singleton
    fun provideArticleDao(database: WanAndroidDatabase): ArticleDao {
        return database.articleDao()
    }

    @Provides
    @Singleton
    fun provideReadHistoryDao(database: WanAndroidDatabase): ReadHistoryDao {
        return database.readHistoryDao()
    }
}