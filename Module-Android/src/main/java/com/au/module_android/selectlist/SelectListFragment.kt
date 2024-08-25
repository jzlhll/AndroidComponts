package com.au.module_android.selectlist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.core.widget.NestedScrollView
import com.au.module_android.ui.FragmentRootActivity
import com.au.module_android.ui.views.ViewToolbarFragment

/**
 * 选择一项。
 */
abstract class SelectListFragment<B: SelectListItem> : ViewToolbarFragment() {
    companion object {
        fun start(context:Context, fragment: Class<SelectListFragment<*>>) {
            FragmentRootActivity.start(context, fragment)
        }
    }

    private lateinit var host:LinearLayout

    /**
     * 标题
     */
    abstract val title:String

    /**
     * 实现：写上所有显示的items
     */
    abstract val items:List<B>

    /**
     * 当前的选中的items
     */
    abstract val initCur:B

    private var mCur:B? = null

    /**
     * 每一行的高度
     */
    abstract fun itemHeight():Int

    /**
     * 每一行的左右边距
     */
    open fun itemPaddingHorz():Int = 0

    /**
     * 每一行的top边距
     */
    open fun itemTopMargin():Int = 0

    override fun hasToolbar(): Boolean {
        return true
    }

    override fun onUiCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        setTitle(title)

        return NestedScrollView(inflater.context).also { scrollView->
            scrollView.isFillViewport = true
            root = scrollView

            val ll = LinearLayout(inflater.context).also {
                host = it
                it.orientation = LinearLayout.VERTICAL
                it.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            }
            scrollView.addView(ll, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT))

            val itemTopMargin = itemTopMargin()
            val itemPaddingHorz = itemPaddingHorz()

            mCur = initCur

            val context = inflater.context

            for (item in items) {
                val v = createItem(context, item, initCur)
                host.addView(
                    v,
                    LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, itemHeight()).also {
                        it.topMargin = itemTopMargin
                        it.leftMargin = itemPaddingHorz
                        it.rightMargin = itemPaddingHorz
                    }
                )
            }
        }
    }

    private fun createItem(context: Context, value: B, cur: B): View {
        val isCur = value == cur

        val v = createItemView(context)
        v.tag = value
        bindItemView(v, value, isCur)
        return v
    }

    abstract fun createItemView(context: Context) : View
    abstract fun bindItemView(v:View, item:B, isSelect:Boolean)

    /**
     * 对于点击后，立刻finish的Fragment，不需要调用该函数。
     * 而需要改变本界面的选择状态，则需要调用。
     */
    open fun afterSelectItem(itemView:View) {
        val oldCur = mCur
        val newCur = itemView.tag

        if (oldCur == newCur) {
            return
        }

        val children = host.children
        for (child in children) {
            if (child.tag is SelectListItem) {
                if (child.tag == oldCur) {
                    bindItemView(child, oldCur as B, false)
                } else if (child.tag == newCur) {
                    bindItemView(child, newCur as B, true)
                }
            }
        }
    }
}