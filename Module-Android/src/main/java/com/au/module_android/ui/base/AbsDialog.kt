package com.au.module_android.ui.base

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.EmptySuper
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import com.au.module_android.screenadapter.ToutiaoScreenAdapter
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.transparentStatusBar

/**
 * @author au
 * @date :2023/11/8 10:59
 * @description: 从中间弹出的样式Dialog
 */
abstract class AbsDialog(private val mode: DialogMode) : AppCompatDialogFragment(), IBaseDialog {
    private var dialogView: View? = null

    override var rootView: View? = null

    override var createdDialog: Dialog? = null

    /**
     * 查找到可以用于toast的ViewGroup。
     */
    override fun findToastViewGroup() : ViewGroup?{
        val rv = rootView
        if (rv is ViewGroup) {
            return rv
        } else if (rv is View) {
            return rv.parent.asOrNull()
        }
        return null
    }

    /**
     * 尽量早一点调用。在show之前。如果是继承，则放在init{}
     */
    override var onDismissBlock:((IBaseDialog)->Unit)? = null

    /**
     * 尽量早一点调用。在show之前。如果是继承，则放在init{}
     */
    override var onShownBlock:((IBaseDialog)->Unit)? = null

    /**
     * 尽量早一点调用。在show之前。如果是继承，则放在init{}
     */
    var onDialogViewLayoutParamBlock: ((contentViewGroup: View, newLp: FrameLayout.LayoutParams) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState).also {
            ToutiaoScreenAdapter.attach(it)
        }

        dialog.setOnShowListener {
            onShownBlock?.invoke(this)
            onShownBlock = null
        }
        createdDialog = dialog
        return dialog
    }

    override fun dismiss() {
        onDismissBlock?.invoke(this)
        onDismissBlock = null
        super.dismiss()
    }

//    override fun dismissNow() {
//        onDismissBlock?.invoke(this as D)
//        onDismissBlock = null
//        super.dismissNow()
//    }

    override fun dismissAllowingStateLoss() {
        onDismissBlock?.invoke(this)
        onDismissBlock = null
        super.dismissAllowingStateLoss()
    }

    /**
     * 对话框的window
     */
    override val window: Window?
        get() = dialog?.window

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //这里也忽略container，因为我们自行在这里rootTouchView.add了， 所以也忽略。
        val contentViewGroup = onCreatingView(inflater, container, savedInstanceState)
        //用于响应触摸外部消失
        val rootTouchView = DialogTouchLayout(inflater.context, this, contentViewGroup)

        rootView = rootTouchView
        dialogView = contentViewGroup

        //设置显示位置
        val oldLp = contentViewGroup.layoutParams
        val newLp = if (oldLp == null) {
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        } else {
            if (oldLp is FrameLayout.LayoutParams) {
                oldLp
            } else {
                FrameLayout.LayoutParams(
                    oldLp.width,
                    oldLp.height
                )
            }
        }
        newLp.gravity = mode.toGravity()

        //设置布局限定
        onDialogViewLayoutParamBlock?.invoke(contentViewGroup, newLp)
        onDialogViewLayoutParamBlock = null

        dialogView = contentViewGroup

        //设置进出动画
        popAnim()

        rootTouchView.addView(contentViewGroup, newLp)
        return rootTouchView
    }

    override fun onStart() {
        super.onStart()
        setWindowStyle()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        createdDialog?.setOnShowListener(null)
    }

    open fun setWindowStyle() {
        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            transparentStatusBar(this)

            attributes = attributes?.also {
                //保证对话框弹出的时候状态栏不是黑色
                it.height = WindowManager.LayoutParams.MATCH_PARENT
                it.width = WindowManager.LayoutParams.MATCH_PARENT
            }
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    /**
     * 设置进出动画。如果你继承修改动画。则无需调用super。
     */
    @EmptySuper
    open fun popAnim() {
        window?.setWindowAnimations(mode.toAnimRes())
    }
}


/**
 * @author au
 * @date :2024/2/19 15:02
 * @description:
 */
internal class DialogTouchLayout(context: Context,
                                     private val dialogFragment: DialogFragment,
                                     private val contentView: View) : FrameLayout(context) {

    private val bounds = RectF()
    private var canDismissWhenDown = false

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
                    dialogFragment.dismiss()
                }
            }
            else -> {
            }
        }
        return true
    }

    private fun checkCanDismiss(ev: MotionEvent): Boolean {
        bounds.set(
            contentView.left.toFloat(),
            contentView.top.toFloat(),
            contentView.right.toFloat(),
            contentView.bottom.toFloat()
        )
        if (!bounds.contains(ev.x, ev.y)) {
            if (dialogFragment.isCancelable) {
                return true
            }
        }
        return false
    }
}