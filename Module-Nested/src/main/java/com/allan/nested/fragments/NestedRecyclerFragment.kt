package com.allan.nested.fragments

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.EmptySuper
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.allan.nested.decoration.LinearLineItemDecoration
import com.allan.nested.layout.NestedRecyclerViewLayout
import com.au.module_android.utils.dp
import com.au.module_android.utils.unsafeLazy
import com.allan.nested.recyclerview.BaseAdapter
import com.au.module_android.ui.views.ViewFragment

/**
 * @author au
 * Date: 2023/2/21
 * Description 使用NestedRecyclerViewLayout作为基础布局。
 *
 */
abstract class NestedRecyclerFragment : ViewFragment() {
    val nestedLayout by unsafeLazy {
        NestedRecyclerViewLayout(requireContext()).also {
            it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    abstract val adapter: BaseAdapter<Any, *>

    /**
     * 给recyclerView添加LayoutManager和ItemDecoration。
     * 默认是竖型的LinearLayoutManager。并且添加ItemDecoration分割横线。
     *
     * 如果有改变。基本上不要调用super，直接复写。
     */
    @EmptySuper
    open fun initRecyclerView(rcv: RecyclerView) {
        addLinear(rcv,
            rcv.context.dp(1f).toInt(),
            Color.parseColor("#eeeeee"),
            drawLastDivider = true)
        rcv.itemAnimator = null
        rcv.updatePadding(top = rcv.context.dp(16f).toInt())
        rcv.clipChildren = false
        rcv.clipToPadding = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView(nestedLayout.recyclerView)
        nestedLayout.recyclerView.adapter = adapter
    }

    override fun onUiCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return nestedLayout
    }

    companion object {
        /**
         * 给recyclerView添加LayoutManger, ItemDecoration
         */
        protected fun addLinear(
            rcv:RecyclerView,
            size: Int,
            @ColorInt color: Int,
            radius: Float = 0f,
            orientation: Int = DividerItemDecoration.VERTICAL,
            startMargin: Int = 0,
            endMargin: Int = 0,
            drawLastDivider: Boolean = false,
        ) {
            val layoutManager = LinearLayoutManager(rcv.context)
            rcv.layoutManager = layoutManager
            rcv.addItemDecoration(
                LinearLineItemDecoration(
                    rcv.context,
                    layoutManager.orientation
                ).also {
                    it.setDrawable(GradientDrawable().also { drawable ->
                        drawable.setColor(color)
                        drawable.cornerRadius = radius
                        drawable.shape = GradientDrawable.RECTANGLE
                        if (orientation == DividerItemDecoration.VERTICAL) {
                            drawable.setSize(-1, size)
                        } else {
                            drawable.setSize(size, -1)
                        }
                    })
                    it.startMargin = startMargin
                    it.endMargin = endMargin
                    it.drawLastDivider = drawLastDivider
                })
        }
    }
}