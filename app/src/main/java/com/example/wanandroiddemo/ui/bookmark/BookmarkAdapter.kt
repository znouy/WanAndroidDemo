package com.example.wanandroiddemo.ui.bookmark

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wanandroiddemo.data.model.domain.Bookmark
import com.example.wanandroiddemo.databinding.ItemBookmarkBinding

class BookmarkAdapter(
    private val onItemClick: (Bookmark) -> Unit,
    private val onEditClick: (Bookmark) -> Unit,
    private val onDeleteClick: (Bookmark) -> Unit
) : ListAdapter<Bookmark, BookmarkAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookmarkBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, onItemClick, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemBookmarkBinding,
        private val onItemClick: (Bookmark) -> Unit,
        private val onEditClick: (Bookmark) -> Unit,
        private val onDeleteClick: (Bookmark) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bookmark: Bookmark) {
            binding.tvName.text = bookmark.name
            binding.tvLink.text = bookmark.link

            binding.root.setOnClickListener { onItemClick(bookmark) }
            binding.btnEdit.setOnClickListener { onEditClick(bookmark) }
            binding.btnDelete.setOnClickListener { onDeleteClick(bookmark) }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<Bookmark>() {
        override fun areItemsTheSame(oldItem: Bookmark, newItem: Bookmark): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Bookmark, newItem: Bookmark): Boolean {
            return oldItem == newItem
        }
    }
}