package com.example.wanandroiddemo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.wanandroiddemo.base.BaseActivity
import com.example.wanandroiddemo.data.local.ThemeManager
import com.example.wanandroiddemo.databinding.ActivityMainBinding
import com.example.wanandroiddemo.databinding.NavHeaderMainBinding
import com.example.wanandroiddemo.ui.auth.LoginActivity
import com.example.wanandroiddemo.ui.bookmark.BookmarkActivity
import com.example.wanandroiddemo.ui.coin.CoinActivity
import com.example.wanandroiddemo.ui.collect.CollectActivity
import com.example.wanandroiddemo.ui.common.AuthGuard
import com.example.wanandroiddemo.ui.history.HistoryActivity
import com.example.wanandroiddemo.ui.search.SearchActivity
import com.example.wanandroiddemo.ui.settings.SettingsActivity
import com.example.wanandroiddemo.ui.share.AddShareActivity
import com.example.wanandroiddemo.ui.share.ShareActivity
import com.example.wanandroiddemo.ui.square.SquareFragment
import com.example.wanandroiddemo.ui.todo.TodoActivity
import com.example.wanandroiddemo.util.createThemeColorStateList
import com.example.wanandroiddemo.util.ext.showConfirmDialog
import com.example.wanandroiddemo.util.setStatusBarColorCompat
import com.example.wanandroiddemo.util.syncNightModeResources
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {
    private lateinit var headerBinding: NavHeaderMainBinding
    private lateinit var navController: NavController
    private val viewModel: MainViewModel by viewModels()

    // 将返回键声明为一个类成员变量，以便动态控制它的开关状态
    private val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawers()
            } else {
                moveTaskToBack(true)
            }
        }
    }

    private val addShareLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                val currentFragment = navHostFragment.childFragmentManager.primaryNavigationFragment
                if (currentFragment is SquareFragment) {
                    currentFragment.refresh()
                }
            }
        }

    @Inject
    lateinit var authGuard: AuthGuard

    override fun getViewBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun onThemeColorChanged(color: Int) {
        // 切换夜间模式，Global Configuration保存的是切换模式后的uimode, MainActivity加载新的uiMode显示,
        // MainActivity 在 onStop 时由局部配置（ Resources）保存切换模式之前的uimode。
        // 从设置界面返回到主页后，由于没有触发“系统级配置变更”（如旋转屏幕），就不会从Global Configuration获取uimode,而是从局部配置获取uiMode
        //这样导致获取到错误的uiMode及Resource

        // 确定真实的日夜状态（不使用 resources.configuration 判定，而是使用你保存在 ViewModel 中的数据）

        val themeColor = ThemeManager.getAdaptiveThemeColor(this, color)
        //读取最新的日/夜间页面底色
        val themeBgColor = ContextCompat.getColor(this, R.color.theme_background)

        // 统一对系统标题栏和侧边栏顶部着色
        binding.toolbar.setBackgroundColor(themeColor)//动态对系统标题栏进行着色
        //通知 DrawerLayout 同步更新状态栏背景色，防止其遮挡状态栏
        binding.drawerLayout.setStatusBarBackgroundColor(themeColor)
        //设置nav_header主题
        headerBinding.root.setBackgroundColor(themeColor)

        //设置页面背景色
        binding.drawerLayout.setBackgroundColor(themeBgColor)
        binding.navView.setBackgroundColor(themeBgColor)
        binding.bottomNavigation.setBackgroundColor(themeBgColor)

        //文本颜色修改
        //根据当前主题动态生成对应的color selector并应用到文本
        val themeColorStateList = createThemeColorStateList(checkedColor = themeColor)
        binding.bottomNavigation.itemIconTintList = themeColorStateList
        binding.bottomNavigation.itemTextColor = themeColorStateList
        //侧滑菜单文本黑白色
        binding.navView.itemTextColor = themeColorStateList
        binding.navView.itemIconTintList = themeColorStateList

    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        //观察是否显示问答
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.showQuestion.collectLatest { isVisible ->
                    val wendaMenuItem = binding.bottomNavigation.menu.findItem(R.id.wendaFragment)
                    wendaMenuItem?.isVisible = isVisible
                    // 如果当前开关被设为了隐藏，且用户当前刚好停留在问答页面上，
                    // 将导航退回首页，防止用户卡在已经被禁用的页面上
                    if (!isVisible && navController.currentDestination?.id == R.id.wendaFragment) {
                        navController.navigate(R.id.homeFragment)
                    }
                }
            }
        }
        // 观察登录状态，动态更新 UI 和 点击行为
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.settingsConfig.distinctUntilChanged() // 如果配置没变，直接拦截，不执行后面的代码（解决返回后执行两次，缓存加重新激活重新手机的数据）
                    .collectLatest { config ->
                        // 当从设置页面修改主题颜色返回时，,主页在不重建的前提下，会自动、瞬间完成自我染色更新
                        Timber.d(
                            "MainActivity 收到配置更新themeColor: ${Integer.toHexString(config.themeColor)}" + ",themeModel:${config.themeModel},themeColor:${config.themeColor}"
                        )
                        val isNight = ThemeManager.isNightMode(this@MainActivity)
                        //强制让 Activity 本身的 Resources 实例应用最新的 Configuration
                        syncNightModeResources(isNight)

                        //强制同步 Activity 的 Resources 配置，将可能被系统回退的局部配置“强行拉回”
                        setStatusBarColorCompat(config.themeColor)
                        onThemeColorChanged(config.themeColor)
                    }

            }

        }

        //观察等级、排名
        lifecycleScope.launch {
            viewModel.userCoinInfo.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { coin ->
                    if (coin != null) {
                        headerBinding.tvInfo.text = "等级:${coin.level} 排名:${coin.rank}"
                    } else {
                        headerBinding.tvInfo.text = "等级:-- 排名:--"
                    }
                }
        }
        lifecycleScope.launch {
            viewModel.userSession.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { session ->
                    val logoutItem = binding.navView.menu.findItem(R.id.nav_logout)
                    logoutItem?.isVisible = session.isLogin // 例如：隐藏退出登录按钮

                    if (session.isLogin) {
                        headerBinding.tvUsername.text = session.userName
                    } else {
                        headerBinding.tvUsername.text = "未登录"
                    }
                    // 更新点击逻辑
                    val clickListener = View.OnClickListener {
                        if (!session.isLogin) {
                            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        }
                    }
                    headerBinding.ivAvatar.setOnClickListener(clickListener)
                    headerBinding.tvUsername.setOnClickListener(clickListener)
                }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        initBottomNavigation()
        initToolBar()
        initNavDrawer()
        //处理返回键
        onBackPressedDispatcher.addCallback(backCallback)

    }

    private fun initNavDrawer() {
        // 确保头像点击也能触发登录
        val headerView = binding.navView.getHeaderView(0)
        headerBinding = NavHeaderMainBinding.bind(headerView)

        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_coin -> { /* 处理积分逻辑 */
                    authGuard.startWith(
                        lifecycleScope, this, Intent(this, CoinActivity::class.java)
                    )
                }

                R.id.nav_collection -> { /* 处理收藏逻辑 */
                    authGuard.startWith(
                        lifecycleScope, this, Intent(this, CollectActivity::class.java)
                    )
                }

                R.id.nav_bookmark -> {
                    authGuard.startWith(
                        lifecycleScope, this, Intent(this, BookmarkActivity::class.java)
                    )
                }

                R.id.nav_share -> { /* 处理分享逻辑 */
                    authGuard.startWith(
                        lifecycleScope, this, Intent(this, ShareActivity::class.java)
                    )
                }

                R.id.nav_history -> { /* 处理历史逻辑 */
                    authGuard.startWith(
                        lifecycleScope, this, Intent(this, HistoryActivity::class.java)
                    )
                }

                R.id.nav_todo -> {
                    authGuard.startWith(
                        lifecycleScope, this, Intent(this, TodoActivity::class.java)
                    )
                }

                R.id.nav_night_mode -> { /* 处理模式逻辑 */
                    viewModel.toggleThemeMode(this@MainActivity)
                }

                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }

                R.id.nav_logout -> {
                    showLogoutDialog()
                }
            }
            binding.drawerLayout.closeDrawers()
            true
        }
    }

    private fun showLogoutDialog() {
        showConfirmDialog("提示", "确定退出登录？") {
            viewModel.logout()
        }
    }

    private fun initToolBar() {
        // 监听目的地变化，手动控制 Toolbar UI
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // 1. 设置标题
            binding.toolbar.title = destination.label

            // 控制菜单图标可见性
            val isSquare = destination.id == R.id.squareFragment
            binding.toolbar.menu.findItem(R.id.action_search)?.isVisible = !isSquare
            binding.toolbar.menu.findItem(R.id.action_add)?.isVisible = isSquare

            binding.toolbar.title = destination.label

        }
        // 监听 Toolbar 点击
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // 设置 Toolbar 菜单点击监听
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_search -> startActivity(Intent(this, SearchActivity::class.java))
                R.id.action_add -> {
                    // 统一委托给 authGuard 进行登录校验与回调启动
                    authGuard.startWith(
                        scope = lifecycleScope,
                        context = this,
                        launcher = addShareLauncher, // 传入定义的 addShareLauncher
                        targetIntent = Intent(this, AddShareActivity::class.java)
                    )
                }
            }
            true
        }
    }

    private fun initBottomNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // 绑定底部导航
        binding.bottomNavigation.setupWithNavController(navController)
    }

}
