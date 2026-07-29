package com.example.wanandroiddemo.ui.about

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import com.example.wanandroiddemo.R
import com.example.wanandroiddemo.base.BaseActivity
import com.example.wanandroiddemo.data.model.domain.Article
import com.example.wanandroiddemo.databinding.ActivityAboutBinding
import com.example.wanandroiddemo.ui.web.ArticleDetailActivity
import com.example.wanandroiddemo.util.ext.versionName
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AboutActivity : BaseActivity<ActivityAboutBinding>() {

    override fun getViewBinding(inflater: LayoutInflater): ActivityAboutBinding {
        return ActivityAboutBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { finish() }

        // 动态拉取系统版本
        binding.tvVersion.text = getString(R.string.about_version_format, versionName)

        // 点击事件
        binding.itemWebsite.setOnClickListener {
            openBrowser("官方网站", "https://www.wanandroid.com")
        }

        binding.itemSourceCode.setOnClickListener {
            openBrowser("项目源码", "https://github.com/zonuy/wanandroid_demo")
        }

        binding.tvBeian.setOnClickListener {
            openBrowser("工信部备案系统", "https://beian.miit.gov.cn")
        }


        binding.itemLicenses.setOnClickListener {
            showLicensesDialog()
        }

        binding.itemDisclaimer.setOnClickListener {
            showDisclaimerDialog()
        }

        binding.itemProtocol.setOnClickListener {
            showProtocolDialog()
        }
    }

    /**
     * 安全调起外部系统浏览器跳转网页
     */
    private fun openBrowser(title: String, url: String) {
        val dummyArticle = Article(
            id = 0,
            title = title,
            link = url,
            author = "关于",
            date = "",
            envelopePic = "",
            category = "官方网站",
            desc = "",
            collect = false // 关于页面调起的网页，不需要开启收藏图标交互
        )
        ArticleDetailActivity.start(this, article = dummyArticle)
    }

    /**
     * 弹出开源许可证书对话框（符合 Google Play 政策要求）
     */
    private fun showLicensesDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.licenses_title))
            .setMessage(getString(R.string.licenses_content))
            .setPositiveButton(getString(R.string.confirm)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    /**
     * 弹出玩 Android 非官方免责声明（符合版权保护要求，撇清官方关系防止下架）
     */
    private fun showDisclaimerDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.disclaimer_title))
            .setMessage(getString(R.string.disclaimer_content))
            .setPositiveButton(getString(R.string.confirm)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    /**
     * 弹出隐私政策与服务条款选择框（符合国内各大应用商店合规审查要求）
     */
    private fun showProtocolDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.protocol_title))
            .setMessage(getString(R.string.protocol_message))
            .setPositiveButton(getString(R.string.protocol_privacy)) { dialog, _ ->
                openBrowser(getString(R.string.protocol_privacy), getString(R.string.url_privacy))
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.protocol_terms)) { dialog, _ ->
                openBrowser(getString(R.string.protocol_terms), getString(R.string.url_terms))
                dialog.dismiss()
            }
            .setNeutralButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, AboutActivity::class.java))
        }
    }
}