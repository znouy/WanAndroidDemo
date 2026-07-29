package com.example.wanandroiddemo.ui.navigation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wanandroiddemo.data.model.domain.Article
import com.example.wanandroiddemo.data.model.domain.NavigationData
import com.example.wanandroiddemo.databinding.ItemSystemBinding
import com.google.android.material.chip.Chip

class NavDetailAdapter(private val onChipClick: ((Article) -> Unit)) :
    RecyclerView.Adapter<NavDetailAdapter.ViewHolder>() {


    private var dataList = listOf<NavigationData>()

    fun submitList(list: List<NavigationData>) {
        dataList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSystemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onChipClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount() = dataList.size

    class ViewHolder(
        private val binding: ItemSystemBinding,
        private val onChipClick: (Article) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: NavigationData) {
            binding.tvTitle.text = data.name

            binding.chipGroup.removeAllViews()
            data.articles.forEach { article ->
                val chip = Chip(binding.root.context).apply {
                    text = article.title
                    setOnClickListener { onChipClick.invoke(article) }
                }
                binding.chipGroup.addView(chip)
            }
        }
    }
}
