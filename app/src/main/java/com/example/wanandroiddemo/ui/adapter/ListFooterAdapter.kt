package com.example.wanandroiddemo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.wanandroiddemo.databinding.ItemListFooterBinding

sealed interface FooterState {
    object Idle : FooterState    // 空闲/不展示（比如列表为空时）
    object Loading : FooterState // 弱网加载中（显示进度条）
    object Error : FooterState   // 加载失败（显示重试）
    object NoMore : FooterState  // 触底底线（显示我是有底线的）
}

/**
 * 专为普通 ListAdapter 设计的通用分页脚布局适配器
 */
class ListFooterAdapter(
    private val onRetry: () -> Unit
) : RecyclerView.Adapter<ListFooterAdapter.ViewHolder>() {

    var currentState: FooterState = FooterState.Idle
        set(value) {
            val previous = field
            if (previous == value) return // 防重锁：状态未变则直接拦截
            field = value                 // 更新幕后字段（Backing Field）

            // 使用无参 when 表达式
            when {
                previous is FooterState.Idle -> notifyItemInserted(0)
                value is FooterState.Idle -> notifyItemRemoved(0)
                else -> notifyItemChanged(0)
            }
        }

    override fun getItemCount(): Int = if (currentState is FooterState.Idle) 0 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListFooterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, onRetry)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentState)
    }

    class ViewHolder(
        private val binding: ItemListFooterBinding, private val onRetry: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.tvError.setOnClickListener { onRetry() }
        }

        fun bind(state: FooterState) {
            binding.layoutLoading.isVisible = state is FooterState.Loading
            binding.tvError.isVisible = state is FooterState.Error
            binding.tvNoMore.isVisible = state is FooterState.NoMore
        }
    }
}