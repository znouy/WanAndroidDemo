package com.example.wanandroiddemo.ui.widget

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.OverScroller
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference
import kotlin.math.abs

/**
 * 基于系统级 [OverScroller] 实现的侧滑菜单容器。
 *
 * ### ⚠️ 布局顺序约定 (重要)：
 * 内部的 2 个子 View 顺序必须严格遵守，否则会导致显示异常：
 * 1. **第一个子 View (Child 0)**：内容区域 (ContentView)，即正常显示在屏幕上的内容。
 * 2. **第二个子 View (Child 1)**：菜单区域 (MenuView)，即侧滑后显示的操作菜单。
 *
 * ### 使用指南：
 * - 该控件会自动检测父容器是否为 [RecyclerView]。
 * - 当列表滚动或触摸到其他侧滑菜单时，会自动处理关闭逻辑，防止多条目同时打开。
 * - **菜单项点击建议**：在菜单项点击事件中，调用 [smoothCloseMenu] 可平滑关闭菜单。
 *
 * ### XML 示例：
 * ```xml
 * <com.example.wanandroiddemo.ui.widget.SwipeMenuLayout
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content">
 *
 *     <!-- 第1个子View：内容区 -->
 *     <TextView android:id="@+id/tv_content" ... />
 *
 *     <!-- 第2个子View：菜单区 -->
 *     <Button android:id="@+id/btn_delete" ... />
 *
 * </com.example.wanandroiddemo.ui.widget.SwipeMenuLayout>
 * ```
 */
class SwipeMenuLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private lateinit var contentView: View
    private lateinit var menuView: View

    private var menuWidth = 0

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val maxVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity

    private val lastP = PointF()
    private val firstP = PointF()

    private val scroller = OverScroller(context)
    private var velocityTracker: VelocityTracker? = null

    // 记忆标签：标记本次触摸手势是否仅仅是为了关闭侧滑
    private var isClosingOtherItem = false

    //是否支持侧滑
    var isSwipeEnable = true
        set(value) {
            field = value
            if (!value) {
                closeMenuDirectly() // 禁用时，确保处于闭合状态
            }
        }

    private var parentRecyclerView: RecyclerView? = null
    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                smoothCloseMenu()
            }
        }
    }

    companion object {
        private var viewCache: WeakReference<SwipeMenuLayout>? = null
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount != 2) {
            throw IllegalArgumentException("SwipeMenuLayout 内部必须且只能有 2 个子 View")
        }
        contentView = getChildAt(0)
        menuView = getChildAt(1)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        isClickable = true

        contentView.measure(widthMeasureSpec, heightMeasureSpec)

        //  如果彻底禁用了侧滑或者菜单被隐藏，直接将菜单宽度置为 0，防止越界测量
        if (!isSwipeEnable || menuView.visibility == GONE) {
            menuWidth = 0
            setMeasuredDimension(contentView.measuredWidth, contentView.measuredHeight)
            return
        }

        val menuWidthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        val menuHeightSpec =
            MeasureSpec.makeMeasureSpec(contentView.measuredHeight, MeasureSpec.EXACTLY)
        menuView.measure(menuWidthSpec, menuHeightSpec)

        menuWidth = menuView.measuredWidth
        setMeasuredDimension(contentView.measuredWidth, contentView.measuredHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        contentView.layout(0, 0, contentView.measuredWidth, contentView.measuredHeight)
        // 优化布局边界控制
        val actualMenuWidth = if (isSwipeEnable) menuWidth else 0
        menuView.layout(
            contentView.measuredWidth,
            0,
            contentView.measuredWidth + actualMenuWidth,
            contentView.measuredHeight
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        var p = parent
        while (p != null) {
            if (p is RecyclerView) {
                parentRecyclerView = p
                p.addOnScrollListener(scrollListener)
                break
            }
            p = p.parent
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        parentRecyclerView?.removeOnScrollListener(scrollListener)
        parentRecyclerView = null
        closeMenuDirectly()
    }

    /**
     * 💡 手势拦截核心
     */
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // 如果禁用了侧滑功能，直接不拦截任何事件！
        if (!isSwipeEnable) {
            return false
        }

        var intercept = false
        val x = ev.x
        val y = ev.y

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                firstP.set(x, y)
                lastP.set(x, y)
                isClosingOtherItem = false

                val cachedView = viewCache?.get()
                if (cachedView != null && cachedView != this) {
                    cachedView.smoothCloseMenu()
                    isClosingOtherItem = true
                    // 🚨 终极修复 1：强行剥夺父容器（RecyclerView/SwipeRefresh）的手势拦截权！
                    parent?.requestDisallowInterceptTouchEvent(true)
                    intercept = true
                }

                if (scrollX > 0 && x < contentView.width - scrollX) {
                    smoothCloseMenu()
                    isClosingOtherItem = true
                    // 🚨 终极修复 2：强行剥夺父容器手势拦截权，彻底锁死上下滑动！
                    parent?.requestDisallowInterceptTouchEvent(true)
                    intercept = true
                }
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = abs(x - firstP.x)
                val dy = abs(y - firstP.y)
                if (dx > touchSlop && dx > dy) {
                    intercept = true
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
            }
        }
        return intercept
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        // 如果禁用了侧滑功能，直接退化回普通容器，不消费任何触摸事件！
        if (!isSwipeEnable) {
            return super.onTouchEvent(ev)
        }

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker?.addMovement(ev)

        val x = ev.x
        val y = ev.y

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!isClosingOtherItem && !scroller.isFinished) {
                    scroller.abortAnimation()
                }
                lastP.set(x, y)
                firstP.set(x, y)
            }

            MotionEvent.ACTION_MOVE -> {
                // 💡 终极修复 3：如果是用来关闭菜单的专属手势，不仅不响应横向滑动，而且在移动期间，
                // 持续向父容器发出禁令，绝对不许下拉刷新或列表滚动偷走事件！
                if (isClosingOtherItem) {
                    parent?.requestDisallowInterceptTouchEvent(true)
                    return true
                }

                val deltaX = lastP.x - x

                if (abs(deltaX) > touchSlop || abs(scrollX) > 0) {
                    parent?.requestDisallowInterceptTouchEvent(true)
                    scrollBy(deltaX.toInt(), 0)

                    if (scrollX < 0) {
                        scrollTo(0, 0)
                    } else if (scrollX > menuWidth) {
                        scrollTo(menuWidth, 0)
                    }
                }
                lastP.set(x, y)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isClosingOtherItem) {
                    isClosingOtherItem = false
                    velocityTracker?.recycle()
                    velocityTracker = null
                    return true
                }

                val dx = abs(x - firstP.x)
                val dy = abs(y - firstP.y)

                if (dx < touchSlop && dy < touchSlop) {
                    if (scrollX > 0) {
                        smoothCloseMenu()
                    } else {
                        performClick()
                    }
                } else {
                    velocityTracker?.computeCurrentVelocity(1000, maxVelocity.toFloat())
                    val xVelocity = velocityTracker?.xVelocity ?: 0f

                    if (abs(xVelocity) > 1000) {
                        if (xVelocity < -1000) {
                            smoothOpenMenu()
                        } else {
                            smoothCloseMenu()
                        }
                    } else {
                        if (scrollX >= menuWidth / 2f) {
                            smoothOpenMenu()
                        } else {
                            smoothCloseMenu()
                        }
                    }
                }

                velocityTracker?.recycle()
                velocityTracker = null
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.currX, 0)
            invalidate()
        }
    }

    fun smoothOpenMenu() {
        if (!isSwipeEnable) return // 禁用时拒绝打开
        viewCache = WeakReference(this)
        scroller.startScroll(scrollX, 0, menuWidth - scrollX, 0, 300)
        invalidate()
    }

    fun smoothCloseMenu() {
        scroller.startScroll(scrollX, 0, -scrollX, 0, 300)
        invalidate()
        if (viewCache?.get() == this) {
            viewCache = null
        }
    }

    fun closeMenuDirectly() {
        if (!scroller.isFinished) {
            scroller.abortAnimation()
        }
        scrollTo(0, 0)
        if (viewCache?.get() == this) {
            viewCache = null
        }
    }
}