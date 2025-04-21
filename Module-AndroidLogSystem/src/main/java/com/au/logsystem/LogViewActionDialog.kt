package com.au.logsystem

import android.content.res.ColorStateList
import androidx.fragment.app.Fragment
import com.au.module_android.Globals
import com.au.module_android.utils.asOrNull
import com.au.module_androidui.dialogs.AbsActionDialogFragment
import com.au.module_androidui.dialogs.FragmentBottomSheetDialog

class LogViewActionDialog : AbsActionDialogFragment() {
    companion object {
        const val VIEW = "view"
        const val DELETE = "delete"
        /**
         * 弹出
         */
        fun pop(f: IAction) {
            if (f is Fragment) {
                FragmentBottomSheetDialog.show<LogViewActionDialog>(f.childFragmentManager)
            }
        }
    }
    private fun normalTextColor() = ColorStateList.valueOf(Globals.getColor(com.au.module_androidcolor.R.color.color_text_normal))
    private val _items = listOf(
        ItemBean(VIEW, "查看", R.drawable.action_view, normalTextColor()),
        ItemBean(DELETE, "删除", R.drawable.action_red_delete)
    )
    override val items: List<ItemBean>
        get() = _items

    interface IAction {
        fun onNotify(mode:String)
    }

    override fun notify(tag: Any) {
        parentFragment?.parentFragment.asOrNull<IAction>()?.onNotify(tag as String)
    }
}