package com.au.module_android.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import com.au.module.android.R
import com.au.module_android.utils.ALog

/**
 * @author allan
 * @date :2023/11/8 10:59
 * @description: 从中间弹出的样式Dialog
 */
abstract class BaseCenterDialog : AppCompatDialogFragment(), IDialogFragment, IUi {
    override val dialogWindowEnterMode = DialogEnterAnimMode.ScaleCenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //外部点击自动隐退父布局root
        val root = TouchDismissView(inflater.context, this)
        rootView = root
        //创建我们想显示的布局，并贴到root上
        contentView = onCreatingView(inflater, null, savedInstanceState)
        root.addView(contentView)

        //设置进入的样式
        val dialogWindow = this.dialogWindow
        if (dialogWindow != null) {
            val anim = when (dialogWindowEnterMode) {
                DialogEnterAnimMode.TopIn -> R.style.PopupWindowTopIn
                DialogEnterAnimMode.BottomIn -> R.style.PopupWindowBottomIn
                DialogEnterAnimMode.ScaleCenter -> R.style.AnimScaleCenter
                else -> null
            }
            if (anim != null) {
                dialogWindow.setWindowAnimations(anim)
            }
        } else {
            ALog.e("dialog window is null")
        }

        return root
    }

//    @CallSuper
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        //if(isTransparentWindow) updateWindowAttributes()
//    }
    /**
     * 透明window属性
     */
//    open fun updateWindowAttributes() {
//        window?.apply {
//            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//            transparentStatusBar(this)
//            attributes = attributes?.also {
//                //保证对话框弹出的时候状态栏不是黑色
//                it.height = WindowManager.LayoutParams.MATCH_PARENT
//                it.width = WindowManager.LayoutParams.MATCH_PARENT
//            }
//            setBackgroundDrawableResource(android.R.color.transparent)
//        }
//    }
}