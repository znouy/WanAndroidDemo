package com.example.wanandroiddemo.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wanandroiddemo.data.repository.LanguageItem
import com.example.wanandroiddemo.databinding.ItemLanguageBinding

class LanguageAdapter(
    private val onItemClick: (LanguageItem) -> Unit
) : ListAdapter<LanguageItem, LanguageAdapter.LanguageViewHolder>(LanguageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LanguageViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LanguageViewHolder(
        private val binding: ItemLanguageBinding,
        private val onItemClick: (LanguageItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LanguageItem) {
            binding.tvDisplayName.text = itemView.context.getString(item.displayName)
            binding.tvNativeName.text = item.nativeName

            // 根据选中状态控制勾选框显示
            binding.ivCheck.isVisible  = item.isSelected

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    private class LanguageDiffCallback : DiffUtil.ItemCallback<LanguageItem>() {
        override fun areItemsTheSame(oldItem: LanguageItem, newItem: LanguageItem): Boolean {
            return oldItem.languageTag == newItem.languageTag
        }

        override fun areContentsTheSame(oldItem: LanguageItem, newItem: LanguageItem): Boolean {
            return oldItem == newItem
        }
    }
}