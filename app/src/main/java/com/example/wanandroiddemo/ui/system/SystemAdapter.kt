package com.example.wanandroiddemo.ui.system

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wanandroiddemo.data.model.domain.SystemCategory
import com.example.wanandroiddemo.databinding.ItemSystemBinding
import com.google.android.material.chip.Chip

class SystemAdapter : ListAdapter<SystemCategory, SystemAdapter.SystemViewHolder>(SystemDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SystemViewHolder {
        val binding = ItemSystemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SystemViewHolder(binding)
    }

    private var onItemClick: ((SystemCategory) -> Unit)? = null

    fun setOnItemClickListener(listener: (SystemCategory) -> Unit) {
        onItemClick = listener
    }

    override fun onBindViewHolder(holder: SystemViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick ?: {})
    }

    class SystemViewHolder(private val binding: ItemSystemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: SystemCategory, onItemClick: (SystemCategory) -> Unit) {
            binding.tvTitle.text = category.name
            binding.root.setOnClickListener { onItemClick(category) }

            binding.chipGroup.removeAllViews()
            category.children.forEach { child ->
                val chip = Chip(binding.root.context).apply {
                    text = child.name
                    setOnClickListener { onItemClick(child) } // 点击子标签
                }
                binding.chipGroup.addView(chip)
            }
        }
    }

    object SystemDiffCallback : DiffUtil.ItemCallback<SystemCategory>() {
        override fun areItemsTheSame(oldItem: SystemCategory, newItem: SystemCategory) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SystemCategory, newItem: SystemCategory) = oldItem == newItem
    }
}