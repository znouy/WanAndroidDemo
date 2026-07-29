package com.example.wanandroiddemo.base

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.example.wanandroiddemo.R
import com.example.wanandroiddemo.data.local.ThemeManager
import com.example.wanandroiddemo.util.setStatusBarColorCompat
import timber.log.Timber

/**
 * 基础 Activity，集成 ViewBinding。
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    protected lateinit var binding: VB

    abstract fun getViewBinding(inflater: LayoutInflater): VB
      override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewBinding(layoutInflater)
        setContentView(binding.root)
        //  启动时，直接拿取 ThemeManager 内存中的颜色进行初次着色
        val themeColor = ThemeManager.getThemeColor()

        initView(savedInstanceState)

        //启动时立即执行一次动态着色，确保初始状态正确

        Timber.d("----------")
        setStatusBarColorCompat(themeColor)
        onThemeColorChanged(themeColor)

        initData()
    }

    /**
     * 初始化控件、设置监听器等
     */
    open fun initView(savedInstanceState: Bundle?) {}
    open fun initData() {

    }

    /**

     * 如果子类的 XML 布局中有一个 ID 叫 "toolbar" 的控件，基类会自动找到它，并将其背景色染成当前的主题色。
     * 子类如果需要自定义其他 View 的变色行为，直接重写此方法即可。
     */
    protected open fun onThemeColorChanged(color: Int) {
        val finalColor = ThemeManager.getAdaptiveThemeColor(this, color)
        binding.root.findViewById<View>(R.id.toolbar)?.setBackgroundColor(finalColor)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val color = ThemeManager.getThemeColor()
        Timber.d("----------")
        setStatusBarColorCompat(color)
        onThemeColorChanged(color)

    }


}
