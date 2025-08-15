package com.au.module_androidui.widget

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.viewbinding.ViewBinding

/**
 * 简易
 */
class SimpleNonRecyclerView<DATA:Any, VB: ViewBinding> : LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var mAdapter: SimpleNonRecyclerAdapter<DATA, VB>? = null
    private var isRefreshing = false

    /**
     * 最上的间距
     */
    var topPadding = 0
    /**
     * 最下的间距
     */
    var bottomPadding = 0

    /**
     * 中间item距离上的距离
     */
    var itemPaddingTop = 0
    /**
     * 中间item距离下的距离
     */
    var itemPaddingBottom = 0

    fun setAdapter(adapter: SimpleNonRecyclerAdapter<DATA, VB>) {
        mAdapter = adapter
    }

    fun submitList(newList: List<DATA>?) {
        mAdapter?.submitList(newList)
    }

    private fun refresh() {
        val adapter = mAdapter ?: return
        isRefreshing = true

        this@SimpleNonRecyclerView.apply {
            //todo 其实可以做点差异化更新 根据viewType决定是否重用该item
            removeAllViews()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                suppressLayout(true)  // 禁止布局更新
                setLayoutTransition(null)  // 禁用过渡动画
            }

            val size = adapter.getItemCount()
            for (i in 0 until size) {
                val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                val binding = adapter.onCreateViewBinding(this, adapter.getItemViewType(i))
                adapter.onBindViewItem(binding, i)
                when (i) {
                    0 -> {
                        lp.topMargin = topPadding
                        lp.bottomMargin = itemPaddingBottom
                    }
                    size - 1 -> {
                        lp.topMargin = itemPaddingTop
                        lp.bottomMargin = bottomPadding
                    }
                    else -> {
                        lp.topMargin = itemPaddingTop
                        lp.bottomMargin = itemPaddingBottom
                    }
                }
                addView(binding.root, lp)
            }

            this.post {
                // 结束批量操作
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    suppressLayout(false)
                }

                if (isAttachedToWindow) {
                    requestLayout()
                    invalidate()
                    isRefreshing = false
                }
            }
        }
    }

    abstract class SimpleNonRecyclerAdapter<DATA:Any, VB: ViewBinding>(val nonRecyclerView: SimpleNonRecyclerView<*, *>) {
        internal var datas:List<DATA>? = null

        abstract fun onCreateViewBinding(parent: SimpleNonRecyclerView<DATA, VB>, viewType: Int) : VB
        abstract fun onBindViewItem(binding: VB, position: Int)
        abstract fun getItemViewType(position: Int): Int
        open fun getItemCount(): Int = datas?.size ?: 0

        open fun submitList(newList: List<DATA>?) {
            this.datas = newList
            nonRecyclerView.refresh()
        }
    }
}