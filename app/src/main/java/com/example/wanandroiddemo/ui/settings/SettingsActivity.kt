package com.example.wanandroiddemo.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.toColorInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.wanandroiddemo.R
import com.example.wanandroiddemo.base.BaseActivity
import com.example.wanandroiddemo.data.local.SettingsConfig
import com.example.wanandroiddemo.databinding.ActivitySettingsBinding
import com.example.wanandroiddemo.ui.about.AboutActivity
import com.example.wanandroiddemo.util.ext.collectMessages
import com.example.wanandroiddemo.util.ext.showConfirmDialog
import com.example.wanandroiddemo.util.setStatusBarColorCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {

    private val viewModel: SettingsViewModel by viewModels()
    override fun getViewBinding(inflater: LayoutInflater): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(inflater)
    }


    override fun initView(savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { finish() }

        // 设置开关状态变化的监听器
        binding.switchBookmark.setOnCheckedChangeListener { isChecked ->
            Timber.e("switchBookmark isChecked  = $isChecked")
            viewModel.setShowBookmarkNotification(isChecked)
        }

        binding.switchQuestion.setOnCheckedChangeListener { isChecked ->
            Timber.e("switchQuestion isChecked  = $isChecked")
            viewModel.setShowQuestion(isChecked)
        }

        binding.switchBanner.setOnCheckedChangeListener { isChecked ->
            Timber.e("switchBanner isChecked  = $isChecked")
            viewModel.setShowBanner(isChecked)
        }

        binding.switchTop.setOnCheckedChangeListener { isChecked ->
            Timber.e("switchTop isChecked  = $isChecked")
            viewModel.setShowTopArticle(isChecked)
        }
        binding.arrowLanguage.setOnClickListener {
            LanguageActivity.start(this)
        }
        binding.arrowTheme.setOnClickListener {
            showThemeColorDialog()
        }
        binding.arrowClearCache.setOnClickListener {
            showClearCacheDialog()
        }
        binding.arrowAbout.setOnClickListener {
            AboutActivity.start(this)
        }
    }

    override fun initData() {
        // 订阅并观察缓存大小的变化
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cacheSize.collectLatest { size ->
                    Timber.d("----------$size-----------")
                    binding.arrowClearCache.setValue(size)
                }
            }
        }

        // 接收最新的设置状态包
        lifecycleScope.launch {
            viewModel.settingsConfig
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest({ config ->
                    handleSetting(config)
                })
        }

        collectMessages(viewModel)

    }

    private fun handleSetting(config: SettingsConfig) {
        Timber.d(
            "SettingActivity 收到配置更新themeColor: ${Integer.toHexString(config.themeColor)}" +
                    ",themeModel:${config.themeModel}"
        )
        // 1. 刷新开关状态
        renderUi(config)
        // 设置主题后更改ui
        Timber.d("----------")
        setStatusBarColorCompat(config.themeColor)
        onThemeColorChanged(config.themeColor)
    }

    fun renderUi(config: SettingsConfig) {
        Timber.d("config = $config")
        binding.arrowLanguage.setValue(getString(viewModel.getCurrentLanguageRes()))

        binding.arrowTheme.setValue(
            if (config.themeColor == -1) Integer.toHexString(R.color.theme_color)
            else Integer.toHexString(config.themeColor)
        )

        // 初始化设置状态
        if (binding.switchBookmark.isChecked() != config.showBookmarkNotification) {
            binding.switchBookmark.setChecked(config.showBookmarkNotification)
        }
        if (binding.switchQuestion.isChecked() != config.showQuestion) {
            binding.switchQuestion.setChecked(config.showQuestion)
        }
        if (binding.switchBanner.isChecked() != config.showBanner) {
            binding.switchBanner.setChecked(config.showBanner)
        }
        if (binding.switchTop.isChecked() != config.showTopArticle) {
            binding.switchTop.setChecked(config.showTopArticle)
        }

    }

    private fun showClearCacheDialog() {
        showConfirmDialog(getString(R.string.setting_clear_cache), "确定要清除缓存吗？") {
            viewModel.clearCache()
        }
    }

    private fun showThemeColorDialog() {
        val colors = intArrayOf(
            "#FF6750A4".toColorInt(),
            "#FFD50000".toColorInt(),
            "#FF00C853".toColorInt(),
            "#FF2962FF".toColorInt()
        )
        val colorNames = arrayOf("Default", "Red", "Green", "Blue")

        AlertDialog.Builder(this)
            .setTitle("Select Theme Color")
            .setItems(colorNames) { _, which ->
                val selectedColor = colors[which]
                viewModel.setThemeColor(selectedColor)
            }
            .show()
    }

}
