package com.au.module_android.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment

/**
 * @author allan
 * @date :2023/11/8 10:59
 * @description: 从中间弹出的样式Dialog
 */
@Deprecated("基础框架的一环，请使用BindingXXXDialog或者ViewXXXDialog")
abstract class AbsCenterDialog : AppCompatDialogFragment(), IDialogFragment {
    final override val dialogWindowEnterMode = DialogEnterAnimMode.ScaleCenter

    private var mRootView:View? = null

    override var rootView: View?
        get() = mRootView
        set(value) {mRootView = value}

    final override fun self() = this

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return onCreateViewSelf(inflater, container, savedInstanceState)
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