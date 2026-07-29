package com.example.wanandroiddemo.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.wanandroiddemo.data.api.ApiService
import com.example.wanandroiddemo.data.local.AppPreferences
import com.example.wanandroiddemo.data.model.domain.CoinRecord
import com.example.wanandroiddemo.data.model.domain.UserCoin
import com.example.wanandroiddemo.data.model.dto.toDomain
import com.example.wanandroiddemo.data.paging.CoinPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoinRepository @Inject constructor(
    private val apiService: ApiService,
    private val appPreferences: AppPreferences
) {
    // 暴露来自最底层的只读可信源
    val localCoinFlow: Flow<UserCoin?> = appPreferences.localCoinFlow

    /**
     * 获取最新个人积分信息，并同步静默刷新 DataStore 缓存
     */
    suspend fun getUserCoin(): UserCoin {
        val response = apiService.getUserCoinInfo()
        if (response.errorCode == 0 && response.data != null) {
            val userCoin = response.data.toDomain()

            // 同步刷新本地 DataStore
            appPreferences.saveUserCoin(userCoin)
            return userCoin
        } else {
            throw Exception(response.errorMsg.ifEmpty { "获取积分失败" })
        }
    }

    // 2. 提供积分分页数据流 (进行数据转换)
    fun getCoinHistoryStream(): Flow<PagingData<CoinRecord>> {
        return Pager(
            config = PagingConfig(pageSize = 15),
            pagingSourceFactory = { CoinPagingSource(apiService) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }
}