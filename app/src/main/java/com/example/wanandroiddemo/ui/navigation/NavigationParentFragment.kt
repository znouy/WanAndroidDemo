package com.example.wanandroiddemo.ui.navigation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.wanandroiddemo.base.BaseFragment
import com.example.wanandroiddemo.databinding.FragmentNavigationParentBinding
import com.example.wanandroiddemo.ui.system.SystemFragment
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NavigationParentFragment : BaseFragment<FragmentNavigationParentBinding>() {

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentNavigationParentBinding.inflate(inflater, container, false)

    override fun initView() {
        // 设置 ViewPager2 的适配器。使用 childFragmentManager 保证生命周期的嵌套安全性
        binding.viewPager.adapter = object : FragmentStateAdapter(childFragmentManager, viewLifecycleOwner.lifecycle) {
            override fun getItemCount(): Int = 2

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> SystemFragment()     // 体系页
                    else -> NavigationFragment() // 导航页
                }
            }
        }

        // 利用 TabLayoutMediator 联动 TabLayout 和 ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "体系"
                else -> "导航"
            }
        }.attach()
    }

    override fun initData() {

    }
}