package com.au.module_androidex.dialog

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
import com.au.module_android.utils.unsafeLazy

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
            //增加fragment高度预设
            val height = this.height
            if (height != null && height > 0) {
                binding.fcv.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
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