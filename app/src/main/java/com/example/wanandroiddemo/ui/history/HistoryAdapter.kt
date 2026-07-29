package com.example.wanandroiddemo.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.wanandroiddemo.R
import com.example.wanandroiddemo.data.model.domain.ReadHistory
import com.example.wanandroiddemo.databinding.ItemHistoryBinding

class HistoryAdapter(
    private val onItemClick: (ReadHistory) -> Unit,
    private val onDeleteClick: (ReadHistory) -> Unit
) :
    PagingDataAdapter<ReadHistory, HistoryAdapter.ViewHolder>(HistoryDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val history = getItem(position)
        history?.let {
            holder.bind(it)
            // 监听整个条目的点击事件，并将点击的 Article 对象回调出去
            holder.itemView.setOnClickListener { onItemClick.invoke(history) }
        }


    }

    class ViewHolder(
        private val binding: ItemHistoryBinding,
        private val onDeleteClick: (ReadHistory) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(history: ReadHistory) {
            binding.tvTitle.text = history.title

            //  展示时间（在列表流中，我们更倾向于精简展示具体时分秒，如 00:35）
            // 如果您的 niceDate 含有完整年月日，我们可以通过 split(" ") 截取时分，体验更佳
            binding.tvReadTime.text = history.readDate

            if (history.isArticle) {
                // 如果是标准文章
                binding.ivTypeIcon.setImageResource(R.drawable.ic_history_article) // 设置文章图标
                binding.tvSubtitle.text = "${history.author}  ·  ${history.category}" // 合并副标题
            } else {
                //如果是普通网页
                binding.ivTypeIcon.setImageResource(R.drawable.ic_history_web)     // 设置网页图标
                binding.tvSubtitle.text = history.link                            // 展示链接网址
            }
            binding.tvDelete.setOnClickListener { onDeleteClick.invoke(history) }
        }
    }

    object HistoryDiffCallback : DiffUtil.ItemCallback<ReadHistory>() {
        override fun areItemsTheSame(oldItem: ReadHistory, newItem: ReadHistory) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ReadHistory, newItem: ReadHistory) =
            oldItem == newItem
    }
}