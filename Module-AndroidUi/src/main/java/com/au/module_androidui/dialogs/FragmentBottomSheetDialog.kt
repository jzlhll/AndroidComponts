package com.au.module_androidui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import com.au.module_android.ui.base.AbsBottomDialog
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.currentStatusBarAndNavBarHeight
import com.au.module_android.utils.getScreenFullSize
import com.au.module_android.utils.serializableCompat
import com.au.module_android.utils.unsafeLazy
import com.au.module_androidui.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.math.min

/**
 * FragmentBottomSheetDialog
 * @param hasEditText 是否有输入框，有输入框，则会安排弹窗的方式有变。
 */
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

    private val fgClass by unsafeLazy {
        arguments?.serializableCompat<Class<Fragment>>("fgClass")
    }
    private val fgBundle by unsafeLazy { arguments?.getBundle("fgBundle") }
    private val height by unsafeLazy { arguments?.getInt("height") }

    private val fragment by unsafeLazy { fgClass?.getDeclaredConstructor()?.newInstance() }
    private val canCancel by unsafeLazy { arguments?.getBoolean("canCancel") ?: true }

    private val paddingMode by unsafeLazy { arguments?.getBoolean("paddingMode", false) ?: false}

    override fun onCreatingView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val layoutId = if (paddingMode) { R.layout.dialog_bottomsheet_padding } else { R.layout.dialog_bottomsheet }
        val root = inflater.inflate(layoutId, container, false)
        val fcv = root.findViewById<FragmentContainerView>(R.id.fcv)

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
                root.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, fixHeight)
            }

            fgBundle?.let {
                fragment.arguments = it
            }

            childFragmentManager.beginTransaction().also {
                it.replace(fcv.id, fragment, null)
                it.commitNow()
            }
        } else {
            dismiss()
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        if (!canCancel) {
            setCancelable(false)
        }
    }
}