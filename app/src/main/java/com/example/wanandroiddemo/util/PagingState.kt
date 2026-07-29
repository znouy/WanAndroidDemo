package com.example.wanandroiddemo.util

/**
 * 统一分页状态机（只读、线程安全、高内聚）
 */
data class PagingState<T>(
    val page: Int = 1,                 // 下一次需要请求的页码（初始值为 1）
    val hasMore: Boolean = true,       // 是否还有更多数据
    val isLoading: Boolean = false,     // 是否正在加载中（防重锁）
    val list: List<T> = emptyList()    // 累计加载的完整数据集
) {
    /**
     * 成功拿到新一页数据，产生下一个新状态
     * @param isRefresh 是否是下拉刷新
     */
    fun toNextSuccess(newItems: List<T>, isRefresh: Boolean): PagingState<T> {
        val updatedList = if (isRefresh) newItems else list + newItems
        // 分页临界点判定：
        // 1. 如果新数据为空，说明绝无更多数据，hasMore 必须为 false。
        // 2. 如果新数据不为空，通过单页阈值（15条）判定是否还有后续页。
        val currentHasMore = if (newItems.isEmpty()) {
            false
        } else {
            newItems.size >= 15 //分页阈值
        }
        return copy(
            page = page + 1,
            hasMore = currentHasMore,
            isLoading = false,
            list = updatedList
        )
    }

    /**
     * 加载失败，重置加载锁
     */
    fun toFailure(): PagingState<T> {
        return copy(isLoading = false)
    }
}