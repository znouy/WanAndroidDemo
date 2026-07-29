package com.example.wanandroiddemo.util.ext

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView 扩展函数：实现丝滑的上拉自动加载更多
 * 整个列表相当于坐标卓，滑动屏幕实是手机这个“镜头”在沿着纸带上下滑动
 *
 * @param preloadIndex 提前几个 Item 开始加载，默认倒数第 4 个开始加载，保障滑动流畅无卡顿
 * @param onLoadMore 触发加载更多时的回调 lambda
 */
fun RecyclerView.onLoadMore(preloadIndex: Int = 4, onLoadMore: () -> Unit) {
    this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            // 只有在向下滑动（dy > 0）时才计算逻辑，避免向上滑动或静止时误触发
            if (dy <= 0) return

            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
            val totalItemCount = layoutManager.itemCount              // 适配器中总共的 Item 数量
            val lastVisibleItemPosition =
                layoutManager.findLastVisibleItemPosition() // 屏幕中最后一个可见的 Position
            // 统一公式，完美通用“触底加载（0）”与“提前预加载（N）”
            if (totalItemCount > 0 && lastVisibleItemPosition >= totalItemCount - 1 - preloadIndex) {
                onLoadMore()
            }

        }
    })

}
