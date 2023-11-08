package com.au.module_android.ui.base

import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.au.module_android.utils.asOrNull

/**
 * @author allan
 * @date :2023/11/8 17:41
 */
interface IDialogFragment {
    /**
     * Dialog的进入方式。
     */
    val dialogWindowEnterMode:DialogEnterAnimMode

    /**
     * 包裹contentView的基础大背景
     */
    var rootView:ViewGroup?

    /**
     * 创建的内容View。就是用来显示的内容
     */
    var contentView: View?

    /**
     * 自身
     */
    fun self():DialogFragment

    /**
     * 对话框的window
     */
    val dialogWindow: Window?
        get() = self().dialog?.window

    /**
     * 查找到可以用于toast的ViewGroup。
     */
    fun findToastViewGroup() : ViewGroup?{
        rootView?.let { tdv->
            val design_bottom_sheet = tdv.parent.asOrNull<ViewGroup>()
            design_bottom_sheet?.let { dbs->
                return dbs.parent.asOrNull<ViewGroup>() //coordinator
            }
        }
        return null
    }
}