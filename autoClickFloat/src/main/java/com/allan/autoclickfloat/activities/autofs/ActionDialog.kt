package com.allan.autoclickfloat.activities.autofs

import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.au.module_android.Globals
import com.au.module_android.utils.asOrNull
import com.au.module_androidui.dialogs.AbsActionDialogFragment
import com.au.module_androidui.dialogs.FragmentBottomSheetDialog

class ActionDialog : AbsActionDialogFragment() {
    enum class ActionMode {
        Edit,
        Delete
    }

    companion object {
        /**
         * 弹出
         */
        fun pop(f: IAction) {
            when (f) {
                is Fragment -> {
                    FragmentBottomSheetDialog.show<ActionDialog>(f.childFragmentManager)
                }

                is AppCompatActivity -> {
                    FragmentBottomSheetDialog.show<ActionDialog>(f.supportFragmentManager)
                }

                else -> {
                    throw IllegalArgumentException("not support $f")
                }
            }
        }

        const val EDIT = "edit"
        const val DELETE = "delete"

        private fun tagToMode(tag: Any): ActionMode {
            return when (tag) {
                EDIT -> ActionMode.Edit
                DELETE -> ActionMode.Delete
                else -> throw IllegalArgumentException("not support $tag")
            }
        }
    }

    interface IAction {
        fun onNotify(mode:ActionMode)
    }

    private fun normalTextColor() = ColorStateList.valueOf(Globals.getColor(com.au.module_androidcolor.R.color.color_text_normal))
    private val _items = listOf(
        ItemBean(EDIT, "编辑", com.allan.autoclickfloat.R.drawable.action_edit, normalTextColor()),
        ItemBean(DELETE, "删除", com.allan.autoclickfloat.R.drawable.action_red_delete)
    )

    override val items: List<ItemBean>
        get() = _items

    override fun notify(tag: Any) {
        parentFragment?.parentFragment.asOrNull<IAction>()?.onNotify(tagToMode(tag))
    }
}