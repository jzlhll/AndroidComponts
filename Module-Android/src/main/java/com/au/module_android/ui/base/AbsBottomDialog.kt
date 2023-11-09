package com.au.module_android.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.au.module_android.utils.asOrNull
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

@Deprecated("基础框架的一环，请使用BindingXXXDialog或者ViewXXXDialog")
abstract class AbsBottomDialog(private val isAlwaysFullScreen:Boolean = false)
        : BottomSheetDialogFragment(), IDialogFragment {
    final override val dialogWindowEnterMode = DialogEnterAnimMode.None

    private var mRootView:View? = null

    final override var rootView: View?
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

    override fun onStart() {
        super.onStart()

        val view = requireView()
        val bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)

        if (isAlwaysFullScreen) { //全屏显示
            dialogWindow?.apply {
                // 1.修改window的高度
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT) // ViewGroup.LayoutParams.MATCH_PARENT
                // 2.修改容器的高度
                val dialog = dialog.asOrNull<BottomSheetDialog>()
                if (dialog != null) {
                    dialog.delegate.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.also {
                        it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                        it.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    }
                }
            }
        }

        //通过如下2个参数，可以让他直接展开到最大，而没有过渡态
        //禁止下滑暂停
        bottomSheetBehavior.skipCollapsed = true
        //设置状态为展开
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}