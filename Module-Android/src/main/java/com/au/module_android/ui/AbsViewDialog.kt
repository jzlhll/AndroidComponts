//package com.au.module_android.ui
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.graphics.RectF
//import android.os.Bundle
//import android.util.AttributeSet
//import android.view.Gravity
//import android.view.LayoutInflater
//import android.view.MotionEvent
//import android.view.View
//import android.view.ViewGroup
//import android.view.Window
//import android.view.WindowManager
//import android.widget.FrameLayout
//import androidx.annotation.StyleRes
//import androidx.appcompat.app.AppCompatDialogFragment
//import com.au.module_android.utils.asOrNull
//
///**
// * @author allan.jiang
// * @date :2023/10/8 17:56
// * @description:
// */
//abstract class AbsViewDialog : AppCompatDialogFragment(), IUi {
//    private var dialogView: View? = null
//    private var touchDismissView:TouchDismissView? = null
//
//    /**
//     * 有的时候，我们需要在dialog上显示toast。则需要从这里找到
//     * 查找到可以用于toast的ViewGroup。
//     */
//    fun findToastViewGroup() : ViewGroup?{
//        val touchDisView = touchDismissView
//        if (touchDisView != null) {
//            //coordinator design_bottom_sheet
//            return touchDisView.parent.asOrNull<ViewGroup>()
//        }
//
//        return null
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        //用于响应触摸外部消失
//        val rootTouchView = TouchDismissView(inflater.context)
//        touchDismissView = rootTouchView
//        //这里也忽略container，因为我们自行在这里rootTouchView.add了， 所以也忽略。
//        val contentViewGroup = onCreatingView(inflater, container, savedInstanceState)
//        dialogView = contentViewGroup
//        val oldLp = contentViewGroup.layoutParams
//        val newLp = if (oldLp == null) {
//            FrameLayout.LayoutParams(
//                FrameLayout.LayoutParams.MATCH_PARENT,
//                FrameLayout.LayoutParams.WRAP_CONTENT
//            ).apply {
//                gravity = Gravity.CENTER
//            }
//        } else {
//            if (oldLp is FrameLayout.LayoutParams) {
//                oldLp
//            } else {
//                FrameLayout.LayoutParams(
//                    oldLp.width,
//                    oldLp.height
//                )
//            }
//        }
//        onRootViewLayoutParams(contentViewGroup, newLp)
//        rootTouchView.addView(contentViewGroup, newLp)
//        return rootTouchView
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        updateWindowAttributes()
//        onViewCreatedFinish(this, savedInstanceState, resources)
//    }
//
//    /**
//     * 触摸外面是否可以取消
//     */
//    var isCanceledOnTouchOutside = true
//
//    /**
//     * 对话框的window
//     */
//    val window: Window?
//        get() = dialog?.window
//
//    /**
//     * 设置窗口动画
//     */
//    fun setWindowAnimations(@StyleRes resId: Int) {
//        window?.setWindowAnimations(resId)
//    }
//
//    /**
//     * 设置布局参数
//     */
//    protected open fun onRootViewLayoutParams(view: View, layoutParams: FrameLayout.LayoutParams) {
//        getMvvmWindowAnimations(view, layoutParams)?.let {
//            setWindowAnimations(it)
//        }
//    }
//
//    /**
//     * 设置对话框进入退出动画
//     */
//    @StyleRes
//    protected open fun getMvvmWindowAnimations(
//        view: View,
//        layoutParams: FrameLayout.LayoutParams
//    ): Int? {
//        return when (layoutParams.gravity) {
//            Gravity.BOTTOM -> {
//                R.style.PopupWindowBottomIn
//            }
//            Gravity.TOP -> {
//                R.style.PopupWindowTopIn
//            }
//            else -> {
//                R.style.AnimScaleCenter
//            }
//        }
//    }
//
//    private fun updateWindowAttributes() {
//        window?.apply {
//            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//            transparentStatusBar()
//            attributes = attributes?.also {
//                //保证对话框弹出的时候状态栏不是黑色
//                it.height = WindowManager.LayoutParams.MATCH_PARENT
//                it.width = WindowManager.LayoutParams.MATCH_PARENT
//            }
//            setBackgroundDrawableResource(android.R.color.transparent)
//        }
//    }
//
//    private inner class TouchDismissView : FrameLayout {
//        constructor(context: Context) : super(context)
//        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
//        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
//            context,
//            attrs,
//            defStyleAttr
//        )
//
//        private val bounds = RectF()
//        var canDismissWhenDown = false
//
//        /**
//         * 不用点击事件处理是防止点击穿透
//         */
//        @SuppressLint("ClickableViewAccessibility")
//        override fun onTouchEvent(ev: MotionEvent?): Boolean {
//            when (ev?.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    canDismissWhenDown = checkCanDismiss(ev)
//                }
//                MotionEvent.ACTION_UP -> {
//                    if (canDismissWhenDown && checkCanDismiss(ev)) {
//                        dismiss()
//                    }
//                }
//                else -> {
//                }
//            }
//            return true
//        }
//
//        private fun checkCanDismiss(ev: MotionEvent): Boolean {
//            val view = dialogView
//            if (view == null) {
//                if (isCancelable && isCanceledOnTouchOutside) {
//                    return true
//                }
//            } else {
//                bounds.set(
//                    view.left.toFloat(),
//                    view.top.toFloat(),
//                    view.right.toFloat(),
//                    view.bottom.toFloat()
//                )
//                if (!bounds.contains(ev.x, ev.y)) {
//                    if (isCancelable && isCanceledOnTouchOutside) {
//                        return true
//                    }
//                }
//            }
//            return false
//        }
//    }
//}