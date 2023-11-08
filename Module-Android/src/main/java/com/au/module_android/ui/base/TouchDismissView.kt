package com.au.module_android.ui.base

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.RectF
import android.view.MotionEvent
import android.widget.FrameLayout

/**
 * 用来控制可以外部点击关闭dialogFragment的父布局
 */
@SuppressLint("ViewConstructor")
class TouchDismissView(context: Context, private val dialogFragment: IDialogFragment) : FrameLayout(context) {
    private val bounds = RectF()
    var canDismissWhenDown = false

    /**
     * 触摸外面是否可以取消
     */
    var isCanceledOnTouchOutside = true

    /**
     * 不用点击事件处理是防止点击穿透
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                canDismissWhenDown = checkCanDismiss(ev)
            }
            MotionEvent.ACTION_UP -> {
                if (canDismissWhenDown && checkCanDismiss(ev)) {
                    dialogFragment.self().dismiss()
                }
            }
            else -> {
            }
        }
        return true
    }

    private fun checkCanDismiss(ev: MotionEvent): Boolean {
        val view = dialogFragment.contentView
        if (view == null) {
            if (dialogFragment.self().isCancelable && isCanceledOnTouchOutside) {
                return true
            }
        } else {
            bounds.set(
                view.left.toFloat(),
                view.top.toFloat(),
                view.right.toFloat(),
                view.bottom.toFloat()
            )
            if (!bounds.contains(ev.x, ev.y)) {
                if (dialogFragment.self().isCancelable && isCanceledOnTouchOutside) {
                    return true
                }
            }
        }
        return false
    }
}