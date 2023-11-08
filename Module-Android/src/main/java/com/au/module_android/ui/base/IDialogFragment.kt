package com.au.module_android.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.au.module.android.R
import com.au.module_android.utils.ALog
import com.au.module_android.utils.asOrNull

/**
 * @author allan
 * @date :2023/11/8 17:41
 */
interface IDialogFragment : IUi {
    /**
     * Dialog的进入方式。
     */
    val dialogWindowEnterMode:DialogEnterAnimMode

    /**
     * 包裹contentView的基础大背景
     */
    var rootView:View?

    /**
     * 自身
     */
    fun self():DialogFragment

    /**
     * 对话框的window
     */
    val dialogWindow: Window?
        get() = self().dialog?.window

    fun onCreateViewSelf(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return onCreatingView(inflater, null, savedInstanceState).also { rootView = it }
    }

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