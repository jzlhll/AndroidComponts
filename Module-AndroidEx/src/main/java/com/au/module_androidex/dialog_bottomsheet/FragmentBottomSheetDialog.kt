package com.au.module_androidex.dialog_bottomsheet

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import com.au.module_android.ui.ViewBackgroundBuilder
import com.au.module_android.ui.base.AbsBottomDialog
import com.au.module_android.utils.BUNDLE_KEY0
import com.au.module_android.utils.BUNDLE_KEY1
import com.au.module_android.utils.BUNDLE_KEY2
import com.au.module_android.utils.BUNDLE_KEY3
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.dp
import com.au.module_android.utils.unsafeLazy
import com.au.module_androidex.R

class FragmentBottomSheetDialog(hasEditText:Boolean = false) : AbsBottomDialog(hasEditText) {
    companion object {
        /**
         * manager 基于哪个fragment的childFragmentManger而弹出。
         * fgBundle 创建的内容Fragment携带了arguments
         * height 弹出的高度
         * isDarkMode 是否是黑暗风格
         */
        inline fun <reified T : Fragment> show(
            manager: FragmentManager,
            fgBundle: Bundle? = null,
            height: Int? = null,
            isDarkMode:Int = 0,
            hasEditText: Boolean = false
        ): FragmentBottomSheetDialog {
            val dialog = FragmentBottomSheetDialog(hasEditText)
            dialog.arguments = Bundle().also {
                it.putSerializable(BUNDLE_KEY0, T::class.java)
                it.putInt(BUNDLE_KEY1, isDarkMode)
                if (fgBundle != null) {
                    it.putBundle(BUNDLE_KEY2, fgBundle)
                }
                if(height != null) it.putInt(BUNDLE_KEY3, height)
            }
            dialog.show(manager, "FragmentContainBottomSheetDialog")
            return dialog
        }
    }
    private val fgClass by unsafeLazy {
        arguments?.getSerializable(BUNDLE_KEY0).asOrNull<Class<Fragment>>()
    }
    private val isDarkMode by unsafeLazy { arguments?.getInt(BUNDLE_KEY1, 0) ?: 0}
    private val fgBundle by unsafeLazy { arguments?.getBundle(BUNDLE_KEY2) }
    private val height by unsafeLazy { arguments?.getInt(BUNDLE_KEY3) }

    private val fragment by unsafeLazy { fgClass?.newInstance() }

    private data class ViewBindingWrap(val root:ViewGroup, val fcv:FragmentContainerView)

    /**
     * Binding变量
     */
    private var mBinding: ViewBindingWrap? = null

    override fun onCreatingView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = when (isDarkMode) {
            1 -> {
                val root = inflater.inflate(R.layout.dialog_bottomsheet, container, false)
                root.background = ViewBackgroundBuilder()
                    .setBackground(Color.parseColor("#1D1D1F"))
                    .setCornerRadius(20f.dp, 20f.dp, 0f, 0f)
                    .build()
                val fcv = root.findViewById<FragmentContainerView>(R.id.fcv)
                ViewBindingWrap(root as ViewGroup, fcv)
            }
            2 -> {
                val root = inflater.inflate(R.layout.dialog_bottomsheet_padding, container, false)
                val fcv = root.findViewById<FragmentContainerView>(R.id.fcv)
                ViewBindingWrap(root as ViewGroup, fcv)
            }
            else -> {
                val root = inflater.inflate(R.layout.dialog_bottomsheet, container, false)
                root.background = ViewBackgroundBuilder()
                    .setBackground(Color.WHITE)
                    .setCornerRadius(20f.dp, 20f.dp, 0f, 0f)
                    .build()
                val fcv = root.findViewById<FragmentContainerView>(R.id.fcv)
                ViewBindingWrap(root as ViewGroup, fcv)
            }
        }
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