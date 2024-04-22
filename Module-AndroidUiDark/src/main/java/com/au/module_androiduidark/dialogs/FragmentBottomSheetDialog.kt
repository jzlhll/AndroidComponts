package com.au.module_androiduidark.dialogs

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import com.au.module_android.ui.ViewBackgroundBuilder
import com.au.module_android.utils.BUNDLE_KEY0
import com.au.module_android.utils.BUNDLE_KEY1
import com.au.module_android.utils.BUNDLE_KEY2
import com.au.module_android.utils.BUNDLE_KEY3
import com.au.module_android.utils.dp
import com.au.module_android.utils.unsafeLazy
import com.au.module_androidex.AbsFragmentBottomSheetDialog
import com.au.module_androiduidark.R

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
            hasEditText: Boolean = false
        ): FragmentBottomSheetDialog {
            val dialog = FragmentBottomSheetDialog(hasEditText)
            dialog.arguments = Bundle().also {
                it.putSerializable(BUNDLE_KEY0, T::class.java)
                it.putBoolean(BUNDLE_KEY1, paddingMode)
                if (fgBundle != null) {
                    it.putBundle(BUNDLE_KEY2, fgBundle)
                }
                if(height != null) it.putInt(BUNDLE_KEY3, height)
            }
            dialog.show(manager, "FragmentContainBottomSheetDialog")
            return dialog
        }
    }

    private val paddingMode by unsafeLazy { arguments?.getBoolean(BUNDLE_KEY1, false) ?: false}

    override fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?): AbsFragmentBottomSheetDialog.ViewBindingWrap {
        return if (paddingMode) {
            val root = inflater.inflate(R.layout.dialog_bottomsheet_padding, container, false)
            val fcv = root.findViewById<FragmentContainerView>(R.id.fcv)
            ViewBindingWrap(root as ViewGroup, fcv)
        } else {
            val root = inflater.inflate(R.layout.dialog_bottomsheet, container, false)
            root.background = ViewBackgroundBuilder()
                .setBackground(Color.parseColor("#1f1f1f"))
                .setCornerRadius(20f.dp, 20f.dp, 0f, 0f)
                .build()
            val fcv = root.findViewById<FragmentContainerView>(R.id.fcv)
            ViewBindingWrap(root as ViewGroup, fcv)
        }
    }
}