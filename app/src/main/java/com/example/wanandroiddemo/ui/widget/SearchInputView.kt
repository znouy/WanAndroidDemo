package com.example.wanandroiddemo.ui.widget
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.example.wanandroiddemo.R
import com.example.wanandroiddemo.databinding.ViewSearchInputBinding
import timber.log.Timber

/**
 * 符合 Material Design 3 (M3) 规范的一键清除式搜索组件
 * * ### 解决的核心痛点
 *  * 1. **物理/手势返回键拦截问题**：彻底消除官方 [androidx.appcompat.widget.SearchView] 强占焦点、
 *  用户需要连续点击多次返回键才能退出的历史遗留体验缺陷。
 *  * 2. 内部自动管理软键盘、光标与 `IME_ACTION_SEARCH` 动作，
 *  对外仅暴露规范、统一的 [OnQueryTextListener] 接口。
 *  *
 *  * ### 支持的 XML 自定义属性 (attrs.xml)
 *  * - `app:showSearchIcon` (boolean, 默认 true) : 是否展示左侧的搜索放大镜图标（会自动联动右侧输入框边距）。
 *  * - `app:queryHint` (string, 默认 "请输入关键词...") : 输入框空状态下的占位提示文本。
 *  *
 *  * ### 示例代码 (XML)
 *  * ```xml
 *  * <com.example.wanandroiddemo.widget.SearchInputView
 *  *     android:id="@+id/searchInput"
 *  *     android:layout_width="match_parent"
 *  *     android:layout_height="38dp"
 *  *     app:showSearchIcon="false"
 *  *     app:queryHint="搜你想要的日程、书签..." />
 *  * ```
 *  *
 *  * ### 示例代码 (Kotlin)
 *  * ```kotlin
 *  * binding.searchInput.setOnQueryTextListener(object : SearchInputView.OnQueryTextListener {
 *  *     override fun onQueryTextSubmit(query: String?): Boolean {
 *  *         val keyword = query.orEmpty().trim()
 *  *         if (keyword.isNotEmpty()) {
 *  *             viewModel.submitSearch(keyword)
 *  *         }
 *  *         return true // 返回 true 会使输入框在内部自动释放焦点、收起软键盘
 *  *     }
 *  *
 *  *     override fun onQueryTextChange(newText: String?): Boolean {
 *  *         if (newText.isNullOrBlank()) {
 *  *             viewModel.loadSuggestions() // 输入框一键清空时，界面瞬间退回热词推荐页
 *  *         }
 *  *         return true
 *  *     }
 *  * })
 *  * setQuery()：填充搜索词并自动移动光标到末尾，并调用onQueryTextSubmit
 */
class SearchInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewSearchInputBinding.inflate(
        LayoutInflater.from(context), this
    )

    // 声明内部接口，完美还原原生双方法设计
    interface OnQueryTextListener {
        fun onQueryTextSubmit(query: String?): Boolean
        fun onQueryTextChange(newText: String?): Boolean
    }

    private var queryTextListener: OnQueryTextListener? = null

    init {
        // 1. 解析自定义属性
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.SearchInputView,
            defStyleAttr,
            0
        )
        val showSearchIcon = typedArray.getBoolean(
            R.styleable.SearchInputView_showSearchIcon,
            true
        )
        val queryHint = typedArray.getString(R.styleable.SearchInputView_queryHint)

        val showSearchText = typedArray.getBoolean(
            R.styleable.SearchInputView_showSearchText,
            true // 默认开启
        )
        val searchText = typedArray.getString(R.styleable.SearchInputView_searchText) ?: "搜索"
        val searchTextColor = typedArray.getColor(
            R.styleable.SearchInputView_searchTextColor,
            Color.WHITE // 默认白色
        )
        typedArray.recycle()

        setSearchIconVisible(showSearchIcon)
        if (!queryHint.isNullOrEmpty()) {
            setQueryHint(queryHint)
        }

        // 应用右侧“搜索”文本按钮配置
        binding.btnSearchText.isVisible = showSearchText
        binding.btnSearchText.text = searchText
        binding.btnSearchText.setTextColor(searchTextColor)

        // 2. 右侧清除按钮一键清空
        binding.ivClear.setOnClickListener {
            binding.etInput.setText("")
        }

        // 3. 文字变动监听：同步控制清除按钮显隐，并回调 onQueryTextChange
        binding.etInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.ivClear.isVisible = !s.isNullOrEmpty()
                // 触发接口回调
                queryTextListener?.onQueryTextChange(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 4. 键盘动作监听：按下搜索时，同步控制软键盘和焦点，并回调 onQueryTextSubmit
        binding.etInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = getQuery()
                hideKeyboard()
                binding.etInput.clearFocus()
                // 触发接口回调
                queryTextListener?.onQueryTextSubmit(query)
                true
            } else {
                false
            }
        }
        // 监听右侧“搜索”文字按钮的直接点击
        binding.btnSearchText.setOnClickListener {
            val query = getQuery()
            // 点击右侧按钮等同于触发了键盘搜索事件
            val isConsumed = queryTextListener?.onQueryTextSubmit(query) ?: false
            if (isConsumed) {
                hideKeyboard()
                clearFocus()
            }
        }
    }
    private var onBackPressListener: (() -> Unit)? = null

    fun setOnBackPressListener(listener: () -> Unit) {
        this.onBackPressListener = listener
    }
    // 💡 2. 软键盘收起、但输入框仍有焦点时：拦截物理返回键（防止系统只执行“清除光标/清除焦点”的废动作）
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        Timber.d("----------------------------------------")
        if (event?.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            //将返回事件交给系统返回键
            context.findActivity()?.onBackPressedDispatcher?.onBackPressed()
            return true //  返回 true 消费事件，防止焦点卡顿
        }
        return super.dispatchKeyEvent(event)
    }
    /**
     * 💡 设置标准搜索监听器
     */
    fun setOnQueryTextListener(listener: OnQueryTextListener) {
        queryTextListener = listener
    }

    /**
     * 控制左侧搜索图标的可见性
     */
    fun setSearchIconVisible(visible: Boolean) {
        binding.ivSearchIcon.isVisible = visible
    }

    /**
     * 动态修改输入框占位提示词
     */
    fun setQueryHint(hint: String) {
        binding.etInput.hint = hint
    }

    /**
     * 设置填充搜索词并移动光标到末尾
     */
    fun setQuery(query: String, triggerSearch: Boolean = true) {
        binding.etInput.setText(query)
        binding.etInput.setSelection(query.length)
        if (triggerSearch) {
            // 主动触发键盘搜索行为并调用监听回调
            val isConsumed = queryTextListener?.onQueryTextSubmit(query) ?: false
            if (isConsumed) {
                hideKeyboard()
                binding.etInput.clearFocus()
            }
        }
    }

    fun getQuery(): String = binding.etInput.text.toString().trim()

    fun hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(binding.etInput.windowToken, 0)
    }
}

/**
 * 安全递归获取当前宿主 Activity
 */
private fun Context.findActivity(): ComponentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) {
            return context
        }
        context = context.baseContext
    }
    return null
}