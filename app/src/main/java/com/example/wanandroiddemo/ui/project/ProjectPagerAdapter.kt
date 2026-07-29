package com.example.wanandroiddemo.ui.project

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.wanandroiddemo.data.model.domain.ProjectCategory
import com.example.wanandroiddemo.ui.article.ArticleListFragment

/**
 * 项目模块 ViewPager2 适配器
 * 这里复用了 ArticleListFragment，因为接口逻辑相似
 */
class ProjectPagerAdapter(
    fragment: Fragment,
    private val categories: List<ProjectCategory>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = categories.size

    override fun createFragment(position: Int): Fragment {
        val category = categories[position]
        return ArticleListFragment().apply {
            arguments = Bundle().apply {
                putInt("cid", category.id)
                putString("title", category.name)
            }
        }
    }
}