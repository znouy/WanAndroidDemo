package com.example.wanandroiddemo.ui.home

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import coil.load
import com.example.wanandroiddemo.R
import com.example.wanandroiddemo.data.model.domain.Banner
import com.example.wanandroiddemo.databinding.ItemBannerBinding
import com.example.wanandroiddemo.databinding.ItemBannerContainerBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Banner 适配器 - 水平无限轮播（采用 Kotlin 协程进行定时任务与流式调用）
 */
class BannerAdapter : RecyclerView.Adapter<BannerAdapter.BannerContainerViewHolder>() {

    private var bannerList: List<Banner> = emptyList()

    fun submitList(list: List<Banner>?) {
        bannerList = list ?: emptyList()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        // 当没有数据时，不在列表中占位 (返回 0)；有数据时，只返回 1 个 Banner 容器 Item
        return if (bannerList.isEmpty()) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerContainerViewHolder {
        val binding = ItemBannerContainerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BannerContainerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BannerContainerViewHolder, position: Int) {
        holder.bind(bannerList)
    }

    override fun onViewAttachedToWindow(holder: BannerContainerViewHolder) {
        super.onViewAttachedToWindow(holder)
        // 划入屏幕时，开启自动轮播
        holder.startAutoScroll()
    }

    override fun onViewDetachedFromWindow(holder: BannerContainerViewHolder) {
        super.onViewDetachedFromWindow(holder)
        // 划出屏幕时，自动暂停轮播，防止内存泄露
        holder.stopAutoScroll()
    }

    // 当 ViewHolder 被回收时，再次确保停止
    override fun onViewRecycled(holder: BannerContainerViewHolder) {
        super.onViewRecycled(holder)
        holder.stopAutoScroll()
    }

    class BannerContainerViewHolder(private val binding: ItemBannerContainerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var autoScrollJob: Job? = null
        private var bannerCount = 0

        fun bind(banners: List<Banner>) {
            bannerCount = banners.size
            if (bannerCount == 0) return

            // 停止任何现有的轮播任务
            stopAutoScroll()

            // 1. 设置内部图片适配器
            val imageAdapter = BannerImageAdapter(banners)
            binding.viewPager.adapter = imageAdapter

            // 2. 无限轮播初始位置（设在中间某个位置，对齐到第0项）
            if (bannerCount > 1) {
                val halfValue = Int.MAX_VALUE / 2
                val initialPos = halfValue - (halfValue % bannerCount)
                binding.viewPager.setCurrentItem(initialPos, false)
            } else {
                binding.viewPager.setCurrentItem(0, false)
            }

            // 3. 设置指示器小圆点
            setupIndicators(banners.size)
            updateIndicator(0)

            // 4. 监听 ViewPager 页面切换与滑动状态
            binding.viewPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    val realPos = if (bannerCount > 0) position % bannerCount else 0
                    updateIndicator(realPos)
                }

                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                    if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                        // 用户拖拽时，停止自动轮播
                        stopAutoScroll()
                    } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                        // 闲置时，重新启动轮播
                        startAutoScroll()
                    }
                }
            })

            // 5. 开启轮播
            startAutoScroll()
        }

        private fun setupIndicators(size: Int) {
            binding.llIndicatorContainer.removeAllViews()
            if (size <= 1) return

            val context = binding.root.context
            val sizePx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                6f,
                context.resources.displayMetrics
            ).toInt()
            val marginPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                4f,
                context.resources.displayMetrics
            ).toInt()

            repeat(size) {
                val view = View(context).apply {
                    val params = LinearLayout.LayoutParams(sizePx, sizePx).apply {
                        setMargins(marginPx, 0, marginPx, 0)
                    }
                    layoutParams = params
                    setBackgroundResource(R.drawable.shape_indicator_normal)
                }
                binding.llIndicatorContainer.addView(view)
            }
        }

        private fun updateIndicator(position: Int) {
            val container = binding.llIndicatorContainer
            if (container.childCount <= position) return
            container.children.forEachIndexed { index, view ->
                if (index == position) {
                    view.setBackgroundResource(R.drawable.shape_indicator_selected)
                } else {
                    view.setBackgroundResource(R.drawable.shape_indicator_normal)
                }
            }
        }

        /**
         * 启动协程自动轮播定时器
         * 1. 自动关联 ViewTree 级的 LifecycleOwner 及其 lifecycleScope
         * 2. 利用 repeatOnLifecycle(Lifecycle.State.RESUMED) 确保只有在页面处于可见并处于前台时才轮播，后台自动挂起节省 CPU 与电量
         * 3. 极其轻量，零线程损耗
         */
        fun startAutoScroll() {
            if (bannerCount <= 1) return
            stopAutoScroll()

            val lifecycleOwner = binding.root.findViewTreeLifecycleOwner()
            val scope = lifecycleOwner?.lifecycleScope

            autoScrollJob = scope?.launch {
                lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    while (isActive) {
                        delay(3000) // 挂起 3 秒，非阻塞式
                        val currentPos = binding.viewPager.currentItem
                        binding.viewPager.setCurrentItem(currentPos + 1, true)
                    }
                }
            }
        }

        /**
         * 取消协程轮播定时器
         */
        fun stopAutoScroll() {
            autoScrollJob?.cancel()
            autoScrollJob = null
        }
    }

    class BannerImageAdapter(private val banners: List<Banner>) :
        RecyclerView.Adapter<BannerImageAdapter.ImageViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val binding = ItemBannerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ImageViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val realPosition = if (banners.isNotEmpty()) position % banners.size else 0
            holder.bind(banners[realPosition])
        }

        override fun getItemCount(): Int {
            return if (banners.isEmpty()) 0 else if (banners.size == 1) 1 else Int.MAX_VALUE
        }

        class ImageViewHolder(private val binding: ItemBannerBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(banner: Banner) {
                // 将宽、高明确设为 MATCH_PARENT
                itemView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                binding.ivBanner.load(banner.imagePath.replace("//www.", "//")) {
                   placeholder(android.R.color.darker_gray)
                    error(android.R.color.holo_red_dark)
                }
            }
        }
    }
}