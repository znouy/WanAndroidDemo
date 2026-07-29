package com.example.wanandroiddemo.ui.navigation

import android.R
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wanandroiddemo.data.model.domain.NavigationData
import com.example.wanandroiddemo.databinding.ItemSystemBinding // 复用简单的列表项布局

class NavCategoryAdapter(private val onCategoryClick: (Int) -> Unit) : 
    ListAdapter<NavigationData, NavCategoryAdapter.ViewHolder>(DiffCallback) {

    private var selectedPosition = 0
    fun getSelectedPosition() = selectedPosition

    fun setSelectedPosition(position: Int) {
        if (selectedPosition != position) {
            val oldPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSystemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position == selectedPosition)
        holder.itemView.setOnClickListener {
            selectedPosition = holder.bindingAdapterPosition
            onCategoryClick(selectedPosition)
            notifyDataSetChanged()
        }
    }

    class ViewHolder(private val binding: ItemSystemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: NavigationData, isSelected: Boolean) {
            binding.tvTitle.text = data.name
            // 设置背景颜色区分
            binding.root.setBackgroundResource(if (isSelected) R.color.white else R.color.transparent)
            // 设置文字颜色区分
            binding.tvTitle.setTextColor(if (isSelected) Color.BLUE else Color.BLACK)
            binding.root.isSelected = isSelected
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<NavigationData>() {
        override fun areItemsTheSame(old: NavigationData, new: NavigationData) = old.cid == new.cid
        override fun areContentsTheSame(old: NavigationData, new: NavigationData) = old == new
    }
}
