# WanAndroid 项目架构与包结构说明文档 (2026 稳定版)

## 1. 架构设计概述 (Architecture Overview)

本工程采用现代 Android 敏捷开发推荐的 **MVVM (Model-View-ViewModel)** 架构模式，融合了 **清洁架构（Clean Architecture）** 的分层思想，并采用更契合复杂业务、高稳定性且迭代摩擦力更低的 **多 Activity + Fragment 混合架构**。

```text
 ┌────────────────────────────────────────────────────────┐
 │                      表现层 (UI Layer)                  │
 │      View (Activity / Fragment)  ◄───[ 监听/收集 ]     │
 │                    │                                   │
 │               [ 行为动作 ]                             │
 │                    ▼                                   │
 │         ViewModel (StateFlow / Event)                  │
 └────────────────────┬───────────────────────────────────┘
                      │ [ 异步请求 / 观察 ]
                      ▼
 ┌────────────────────────────────────────────────────────┐
 │                      数据层 (Data Layer)                │
 │                 Repository (单点调度)                   │
 │                    │                                   │
 │         ┌──────────┴──────────┐                        │
 │         ▼                     ▼                        │
 │    Local Storage          Network API                  │
 │    (Room/DataStore)       (Retrofit DTO)               │
 └────────────────────────────────────────────────────────┘
```

*   **UI 表现层**：采用 XML Layout + ViewBinding。主页面通过 `MainActivity` 承载 `BottomNavigationView` 的 Tab 切换与侧滑 `DrawerLayout`；二级独立业务页面采用独立的二级 Activity，使用 `StateFlow` 进行生命周期安全的响应式状态监听。
*   **业务逻辑层**：使用 Jetpack `ViewModel` 隔离界面逻辑。将所有异步交互或点击行为抽象为 `Action` 传回 ViewModel，实现单向数据流（UDF）闭环。
*   **核心数据层**：采用 `Repository` 模式作为单一数据信源调度中心。由仓库层统一调度本地缓存数据库（Room）、系统偏好配置（DataStore）以及网络请求（Retrofit）。
*   **依赖注入**：全量使用 **Hilt** 进行依赖注入。将对象的实例化规则全部收拢至 `di` 文件夹中，实现业务代码与具体组件实例化逻辑的高效解耦。

###  核心高阶架构方案对齐

*   **组合类委托体系与生命周期对齐**：
    为了彻底避免基类膨胀，项目引入了 Kotlin 类委托（`by`）与 Hilt 依赖注入。
   *   **ViewModel 层级委托**：`MessageDelegate`（全局消息发送）与 `CollectDelegate`（收藏状态同步）均在 **ViewModel 层**采用 Kotlin 类委托（`by`）装载。ViewModel 在 Hilt 的 `ViewModelComponent` 中通过构造函数注入自动绑定实现类，View 只需要通过单行扩展函数进行事件收集监听即可。
   *   **View 层级委托**：`LoadingDelegate`（全局加载转圈）在 **Activity/Fragment 表现层**进行装载。为了规避父类未构造完毕时强行读取 `FragmentManager` 导致的崩溃，在 UI 页面中使用时**必须通过 `by lazy` 延迟代理实例化** [2]。
*   **自由的双模分页方案**：
    项目提供 **`Paging 3`** 与 **`PagingState` 手动状态机** 两套标准分页工具。**只要有上拉加载和下拉刷新的页面，开发人员可以根据具体的场景和交互复杂度自由、无限制地选用其中任何一种实现。**

---

## 2. 完整目录树结构 (Directory Tree Structure)

以下为本应用 `app/src/main/` 路径下的完整物理分包与文件结构。请直接对齐该结构开展后续开发：

```text
com.example.wanandroiddemo
│
│   # 混合架构主入口： hospital 级别稳定，承载主页 Tab 切换与侧滑 Drawer 抽屉的染色更新
│   MainActivity.kt             
│   MainViewModel.kt            
│   WanAndroidApp.kt            # 自定义 Application，用于 Hilt 初始化及全局配置
│
+---base                        # 基础框架层，提供通用的基类抽象（保持极简不膨胀）
│       BaseActivity.kt         # 基类 Activity，封装 ViewBinding 与通用主题染色接口
│       BaseFragment.kt         # 基类 Fragment，封装 ViewBinding 与生命周期安全处理
│
+---data                        # 核心数据层，负责全应用的数据调度与持久化
│   +---api                     
│   │       ApiService.kt       # Retrofit 网络接口层，统一定义 API 契约
│   │
│   +---database                # Room 本地持久化
│   │   |   WanAndroidDatabase.kt # Room 数据库配置类
│   │   |
│   │   \---dao                 # 数据操作 DAO 接口
│   │           ArticleDao.kt
│   │           ReadHistoryDao.kt
│   │
│   +---local                   # 本地偏好设置与系统状态持久化管理
│   │       AppPreferences.kt   # Jetpack DataStore 配置，保存用户登录态/主题/开关 [1]
│   │       LocalCookieJar.kt   # Retrofit Cookie 自动拦截持久化，维持长登录状态
│   │       LocaleManager.kt    # 全局多语言环境控制器
│   │       ThemeManager.kt     # 全局动态着色与日夜主题状态机
│   │
│   +---model                   # 数据模型定义包（遵从清洁架构，进行职责硬隔离）
│   │   +---domain              # 领域模型：UI 直接消费的纯净 Kotlin 对象
│   │   │       Article.kt
│   │   │       Banner.kt       # 💡 优化：已归入 Domain 包
│   │   │       Bookmark.kt
│   │   │       CoinModels.kt
│   │   │       HotKey.kt
│   │   │       NavigationData.kt
│   │   │       ProjectCategory.kt
│   │   │       ReadHistory.kt
│   │   │       SystemCategory.kt
│   │   │       Todo.kt
│   │   │       User.kt
│   │   │
│   │   +---dto                 # 💡 传输模型：与网络接口响应 JSON 格式完全一致
│   │   │       ArticleDto.kt
│   │   │       BaseResponse.kt
│   │   │       BookmarkDto.kt
│   │   │       CollectArticleDto.kt
│   │   │       HomeArticleDto.kt
│   │   │       HotKeyDto.kt
│   │   │       NavigationDto.kt
│   │   │       PageResponse.kt
│   │   │       ProjectCategoryDto.kt
│   │   │       ShareArticleDto.kt
│   │   │       SystemCategoryDto.kt
│   │   │       TodoDto.kt
│   │   │       UserCoinDto.kt
│   │   │       UserDto.kt
│   │   │
│   │   \---entity              # 💡 存储模型：本地数据库表映射实体
│   │           ArticleEntity.kt
│   │           ReadHistoryEntity.kt
│   │
│   +---paging                  # Jetpack Paging 3 分页组件数据流源
│   │       ArticlePagingSource.kt
│   │       CoinPagingSource.kt
│   │       CollectPagingSource.kt
│   │       SearchPagingSource.kt
│   │       SharePagingSource.kt
│   │       SquarePagingSource.kt
│   │       SystemArticlePagingSource.kt
│   │       WendaPagingSource.kt
│   │
│   \---repository              # 仓库层，数据流向的唯一调度中心
│           AuthRepository.kt
│           BookmarkRepository.kt
│           CoinRepository.kt
│           CollectRepository.kt
│           HistoryRepository.kt
│           HomeRepository.kt
│           LanguageRepository.kt
│           NavigationRepository.kt
│           ProjectRepository.kt
│           SearchRepository.kt
│           SettingsRepository.kt
│           ShareRepository.kt
│           SquareRepository.kt
│           SystemRepository.kt
│           TodoRepository.kt   # 💡 优化：已成功从 ui/todo 移动合流至数据层
│           WendaRepository.kt
│
+---di                          # Hilt 依赖注入配置模块
│       DatabaseModule.kt       # 数据库实例与 Dao 自动提供者
│       DelegateModule.kt       # 收藏/消息委托接口绑定
│       MessageModule.kt        # 消息提示相关依赖注入
│       NetworkModule.kt        # Retrofit & OkHttpClient 全局管理
│
+---ui                          # 业务表现层，按业务特征分包（Activity 与 Fragment 混合架构）
│   +---about                   # 关于我们
│   │       AboutActivity.kt    # 
│   │
│   +---adapter                 # 全局共享适配器集合（只存放确定多处复用的适配器）
│   │       ArticleAdapter.kt   # 通用文章列表适配器
│   │       BannerAdapter.kt    # 首页 Banner 轮播适配器
│   │       ListFooterAdapter.kt# 高性能局部刷新分页脚适配器
│   │       NavCategoryAdapter.kt
│   │       NavDetailAdapter.kt
│   │       ProjectPagerAdapter.kt
│   │       SystemAdapter.kt
│   │
│   +---article                 # 二级列表页面模块
│   │       ArticleListActivity.kt
│   │       ArticleListFragment.kt
│   │       ArticleListViewModel.kt
│   │
│   +---auth                    # 登录认证授权模块
│   │       AuthViewModel.kt
│   │       LoginActivity.kt    # 
│   │       SplashActivity.kt   # 闪屏页 Activity
│   │
│   +---bookmark                # 书签管理模块
│   │       BookmarkActivity.kt # 
│   │       BookmarkAdapter.kt  # 
│   │       BookmarkViewModel.kt
│   │       EditBookmarkDialogFragment.kt
│   │
│   +---coin                    # 积分系统模块
│   │       CoinActivity.kt
│   │       CoinHistoryAdapter.kt 
│   │       CoinViewModel.kt
│   │
│   +---collect                 # 我的收藏列表
│   │       CollectActivity.kt
│   │       CollectViewModel.kt
│   │
│   +---common                  # 跨模块共享逻辑
│   │   |   AuthGuard.kt        # 登录认证校验与路由拦截守卫
│   │   |
│   │   \---delegate            # 功能代理实现（类委托）
│   │           CollectDelegate.kt
│   │           CollectDelegateImpl.kt
│   │           MessageDelegate.kt
│   │           MessageDelegateImpl.kt
│   │
│   +---history                 # 浏览历史
│   │       HistoryActivity.kt
│   │       HistoryAdapter.kt   
│   │       HistoryViewModel.kt
│   │
│   +---home                    # 首页
│   │       HomeFragment.kt
│   │       HomeViewModel.kt
│   │
│   +---navigation              # 导航体系
│   │       NavigationFragment.kt
│   │       NavigationParentFragment.kt
│   │       NavigationViewModel.kt
│   │
│   +---project                 # 项目分类
│   │       ProjectFragment.kt
│   │       ProjectViewModel.kt
│   │
│   +---search                  # 全局搜索
│   │       SearchActivity.kt   
│   │       SearchViewModel.kt
│   │
│   +---settings                # 设置中心
│   │       LanguageActivity.kt 
│   │       LanguageAdapter.kt
│   │       LanguageViewModel.kt
│   │       SettingsActivity.kt 
│   │       SettingsViewModel.kt
│   │
│   +---share                   # 分享模块
│   │       AddShareActivity.kt
│   │       AddShareViewModel.kt
│   │       ShareActivity.kt
│   │       ShareViewModel.kt
│   │
│   +---square                  # 广场模块
│   │       SquareFragment.kt
│   │       SquareViewModel.kt
│   │
│   +---system                  # 知识体系
│   │       SystemFragment.kt
│   │       SystemViewModel.kt
│   │
│   +---todo                    # 待办日程管理
│   │       EditTodoDialogFragment.kt
│   │       TodoActivity.kt     
│   │       TodoAdapter.kt      
│   │       TodoViewModel.kt    
│   │
│   +---web                     # 网页容器
│   │       ArticleDetailActivity.kt
│   │       ArticleDetailFragment.kt
│   │       ArticleDetailViewModel.kt
│   │
│   +---wenda                   # 问答
│   │       WendaFragment.kt
│   │       WendaViewModel.kt
│   │
│   \---widget                  # 自定义 UI 控件库（不与具体业务产生耦合）
│       |   AppWebView.kt       
│       |   PagingRecyclerView.kt
│       |   SearchInputView.kt
│       |   SettingArrowView.kt
│       |   SettingSwitchView.kt
│       |   StateLayout.kt
│       |   SwipMenuLayout.kt
│       |
│       \---loading             # 全局加载 Dialog 代理组件
│               LoadingDelegate.kt
│               LoadingDelegateImpl.kt
│               LoadingDialogFragment.kt
│
+---util                        # 通用辅助与基础设施工具包（扁平化结构）
│   |   CacheManager.kt
│   |   DateUtil.kt
│   |   LogManager.kt
│   |   PagingState.kt         
│   |   ThemeExt.kt
│   │
│   \---ext                     # Kotlin 扩展函数集
│           ActivityExt.kt
│           AppExt.kt
│           ContextToastExt.kt
│           DimensExt.kt
│           FileExt.kt
│           FragmentExt.kt
│           NetworkExt.kt
│           RecyclerViewExt.kt  # 上拉预加载监听
│           ResultExt.kt
│           StringExt.kt
│           ThrowableExt.kt
│           ViewExt.kt
```

---

## 3. 分包职责与日常开发规范

为了保持该架构的长期健康度，开发过程中请遵循以下规范：

### 3.1 `widget`（自定义控件）的准入守则
*   **严禁写入业务数据**。`widget` 包下的自定义 View、Layout、以及 Dialog，在设计上必须是一个**绝对的“逻辑黑盒”**。
*   `widget` 下的自定义 View 只能通过对外暴露 `Setters`、`XML 属性` 或 `Callback 监听` 与外部通信。
*   不能在这个包里直接调用网络接口、数据库，或直接引用业务 DTO 及 Domain 实体。

### 3.2 页面新增 Adapter 放在哪里？
*   **原则**：先判断是“独占”还是“共享”。
*   如果新增的 Adapter **仅在某个页面使用**（如广场页面的分类 Tag 列表、日程待办列表等），必须直接放在其功能 Feature 文件夹内（如 `ui/todo/TodoAdapter.kt`）。这保证了模块的自包含，降低了物理文件的迁移与重构成本。
*   如果新增的 Adapter **会被两个以上的 Fragment/Activity 使用**（如通用的文章列表 `ArticleAdapter`），则应将其放入 `ui/adapter` 中。

### 3.3 数据流向与数据模型（Model）转换规则
*   **数据流动路径**：`ApiService` -> `Network DTO` -> `Repository` -> 转换为 `Domain Model` (或 `Entity` 存入数据库后转化为 `Domain Model`) -> `ViewModel` 包装为 `UI State` -> `View(Fragment/Activity)` 渲染。
*   尽可能在 `Repository` 层（数据源边界处）使用 **Kotlin 扩展函数** 完成网络实体（DTO）到本地实体（Entity/Domain）的映射清洗。
*   **绝不将 `BaseResponse` 或任何带有网络框架注解（如 Moshi 注解等）的原始网络 DTO 直接暴露给 ViewModel 或表现层（View/Fragment/Activity）**，以此从物理上阻断网络层字段变更直接污染 UI 的隐患。

### 3.4 资源文件命名规范 (`res/`)
*   **Layout 布局文件**：统一使用 `类型_模块名_功能.xml` 进行扁平化命名，例如：`fragment_home.xml`，`item_article.xml`，`dialog_edit_todo.xml`。
*   **ID 命名**：统一使用下划线蛇形命名法（小写下划线），并在最前缀表明具体控件类型。例如：按钮 `btn_login`，输入框 `et_username`，列表 `rv_article_list`，容器布局 `layout_suggestion`。
*   **字符资源硬编码清理**：网络链接、弹窗文本、长段免责声明和开源许可协议等一律收拢至 `strings.xml` 中，**禁止在 Activity/Fragment 中手写长段汉字硬编码**。字符资源中的符号 `&` 必须写为转义符 `&amp;` [2]；需要换行的长文本必须在 XML 中使用 `\n` 进行转义，以确保编译正常。

### 3.5 禁止在 View (Activity / Fragment) 层编写任何 `try-catch` 块
*   所有由于用户点击而引发的网络挂起动作，统一在 ViewModel 协程启动的最外层，使用 **`coRunCatching`** 进行安全异常捕获 [5]。
*   捕获到的底层网络异常，在仓库层边缘通过 **`mapNetworkException()`** 翻译为语义友好提示。
*   **View 层仅作为被动观察者**：接收来自 ViewModel 的 `UiState.Error` 状态并展示错误页面（`stateLayout.showError`），或接收 `MessageDelegate` 喷出的单次消息事件展示 Toast，从而在物理上消灭 UI 层的手写捕获代码。