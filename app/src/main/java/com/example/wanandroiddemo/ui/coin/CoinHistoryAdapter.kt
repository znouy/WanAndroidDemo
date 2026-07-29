package com.example.wanandroiddemo.ui.coin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.wanandroiddemo.data.model.domain.CoinRecord
import com.example.wanandroiddemo.databinding.ItemCoinHistoryBinding

/**
 * 💡 积分获取历史列表适配器（Paging 3 版本）
 * 绑定我们刚才在 Domain 里清洗好的 CoinRecord 干净数据模型
 */
class CoinHistoryAdapter : PagingDataAdapter<CoinRecord, CoinHistoryAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCoinHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    class ViewHolder(
        private val binding: ItemCoinHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(record: CoinRecord) {
            binding.tvReason.text = record.reason
            binding.tvDate.text = record.date
            binding.tvCoinCount.text = record.coinCount

            // 💡 商业级设计细节：根据积分的增减显示不同的颜色
            // 增加积分显示主题蓝色（#2F74E5），减少积分显示中灰色（#999999）
            if (record.coinCount.startsWith("+")) {
                binding.tvCoinCount.setTextColor(0xFF2F74E5.toInt())
            } else {
                binding.tvCoinCount.setTextColor(0xFF999999.toInt())
            }
        }
    }

    /**
     * Paging 3 底层自动差异化计算（比对新旧数据）
     */
    object DiffCallback : DiffUtil.ItemCallback<CoinRecord>() {
        override fun areItemsTheSame(oldItem: CoinRecord, newItem: CoinRecord): Boolean {
            return oldItem.id == newItem.id // 比对唯一 ID
        }

        override fun areContentsTheSame(oldItem: CoinRecord, newItem: CoinRecord): Boolean {
            return oldItem == newItem // 比对整个数据类内容是否发生变化
        }
    }
}