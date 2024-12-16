package com.au.module_androidui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.dialog.AbsFragmentBottomSheetDialog
import com.au.module_androidui.R

class FragmentBottomSheetDialog(hasEditText:Boolean = false) : AbsFragmentBottomSheetDialog(hasEditText) {
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
            paddingMode:Boolean = false,
            hasEditText: Boolean = false,
            canCancel:Boolean = true,
        ): FragmentBottomSheetDialog {
            val dialog = FragmentBottomSheetDialog(hasEditText)
            dialog.arguments = Bundle().also {
                it.putSerializable("fgClass", T::class.java)
                it.putBoolean("paddingMode", paddingMode)
                if (fgBundle != null) {
                    it.putBundle("fgBundle", fgBundle)
                }
                if(height != null) it.putInt("height", height)
                it.putBoolean("canCancel", canCancel)
            }
            dialog.show(manager, "FragmentContainBottomSheetDialog")
            return dialog
        }
    }

    private val paddingMode by unsafeLazy { arguments?.getBoolean("paddingMode", false) ?: false}

    override fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?): ViewBindingWrap {
        return if (paddingMode) {
            val root = inflater.inflate(R.layout.dialog_bottomsheet_padding, container, false)
            val fcv = root.findViewById<FragmentContainerView>(R.id.fcv)
            ViewBindingWrap(root as ViewGroup, fcv)
        } else {
            val root = inflater.inflate(R.layout.dialog_bottomsheet, container, false)
            val fcv = root.findViewById<FragmentContainerView>(R.id.fcv)
            ViewBindingWrap(root as ViewGroup, fcv)
        }
    }
}