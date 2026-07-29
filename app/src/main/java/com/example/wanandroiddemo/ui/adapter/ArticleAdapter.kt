package com.example.wanandroiddemo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.wanandroiddemo.R
import com.example.wanandroiddemo.data.model.domain.Article
import com.example.wanandroiddemo.databinding.ItemArticleBinding

/**
 * 文章列表适配器
 */
class ArticleAdapter(
    private val onCollectClick: (Article) -> Unit,
) :
    PagingDataAdapter<Article, ArticleAdapter.ArticleViewHolder>(ArticleDiffCallback) {
    // 声明一个可空的点击监听属性，默认值为 null
    private var onItemClickListener: ((Article) -> Unit)? = null


    // 提供一个公开的设置监听器的方法（供需要点击的 Fragment 调用）
    fun setOnItemClickListener(listener: (Article) -> Unit) {
        this.onItemClickListener = listener
    }

    var isSwipeEnabled: Boolean = false // 默认关闭侧滑
    var onDeleteClick: ((Article) -> Unit)? = null // 删除回调


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticleViewHolder(binding, onCollectClick, isSwipeEnabled, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = getItem(position)
        article?.let {
            holder.bind(it)
            // 监听整个条目的点击事件，并将点击的 Article 对象回调出去
            holder.itemView.setOnClickListener { onItemClickListener?.invoke(article) }
        }


    }

    class ArticleViewHolder(
        private val binding: ItemArticleBinding,
        private val onCollectClick: (Article) -> Unit,
        private val isSwipeEnabled: Boolean,
        private val onDeleteClick: ((Article) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(article: Article) {
            binding.tvTitle.text = article.title
            binding.tvAuthor.text = article.author
            binding.tvDate.text = article.date
            binding.tvCategory.text = article.category
            binding.tvTopLabel.isVisible = article.isTop
            if (article.collect) {
                // 已收藏，显示红色实心爱心
                binding.ivCollect.setImageResource(R.drawable.ic_favorite_red)
            } else {
                // 未收藏（或取消收藏后），显示灰色空心爱心
                binding.ivCollect.setImageResource(R.drawable.ic_favorite_grey)
            }
            binding.ivCollect.setOnClickListener { onCollectClick(article) }
            binding.tvDelete.isVisible = isSwipeEnabled
            binding.root.isSwipeEnable = isSwipeEnabled
            binding.tvDelete.setOnClickListener {
                onDeleteClick?.invoke(article)
            }
        }
    }

    object ArticleDiffCallback : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Article, newItem: Article) = oldItem == newItem
    }
}
