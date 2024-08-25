package com.au.module_android.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.au.module_android.ui.base.AbsBottomDialog
import com.au.module_android.utils.BUNDLE_KEY0
import com.au.module_android.utils.BUNDLE_KEY2
import com.au.module_android.utils.BUNDLE_KEY3
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.currentStatusBarAndNavBarHeight
import com.au.module_android.utils.getScreenFullSize
import com.au.module_android.utils.unsafeLazy
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.math.min

abstract class AbsFragmentBottomSheetDialog(hasEditText:Boolean = false) : AbsBottomDialog(hasEditText) {
    private val fgClass by unsafeLazy {
        arguments?.getSerializable(BUNDLE_KEY0).asOrNull<Class<Fragment>>()
    }
    private val fgBundle by unsafeLazy { arguments?.getBundle(BUNDLE_KEY2) }
    private val height by unsafeLazy { arguments?.getInt(BUNDLE_KEY3) }

    private val fragment by unsafeLazy { fgClass?.newInstance() }

    data class ViewBindingWrap(val root:ViewGroup, val fcv:FragmentContainerView)

    /**
     * Binding变量
     */
    private var mBinding: ViewBindingWrap? = null

    protected abstract fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?) : ViewBindingWrap

    override fun onCreatingView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = createViewBinding(inflater, container)
        mBinding = binding

        val fragment = this.fragment
        if (fragment != null) {
            //最大高度限定实现
            val screenSize = requireActivity().getScreenFullSize()
            val staAndNavHeight = requireActivity().currentStatusBarAndNavBarHeight() //在dialog中使用。已经渲染好了。这肯定是ok了的。
            val statusBarHeight = staAndNavHeight?.first ?: 0
            val navigationBarHeight = staAndNavHeight?.second ?: 0

            val maxHeight:Int = screenSize.second - statusBarHeight
            val height = this.height ?: 0

            if (height == 0) {
                dialog.asOrNull<BottomSheetDialog>()?.behavior?.let { behavior->
                    behavior.maxHeight = maxHeight
                }
            } else {
                //fragment的高度height + 补充navigationbar的高度
                val targetHeight = height + (if (isPaddingNavigationBarHeight) navigationBarHeight else 0)
                val fixHeight = min(targetHeight, maxHeight)
                binding.root.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, fixHeight)
            }

            fgBundle?.let {
                fragment.arguments = it
            }

            childFragmentManager.beginTransaction()
                .replace(binding.fcv.id, fragment, null)
                .commit()
        } else {
            dismiss()
        }

        return binding.root
    }
}