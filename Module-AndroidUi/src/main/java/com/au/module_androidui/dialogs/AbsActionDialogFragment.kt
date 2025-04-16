package com.au.module_androidui.dialogs

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Space
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.au.module_android.click.onClick
import com.au.module_android.ui.views.ViewFragment
import com.au.module_android.utils.asOrNull
import com.au.module_androidui.databinding.LeftIconAndTextBinding

/**
 * 左边文字，右边勾勾，点击一项立刻dismiss的对话框基类
 */
abstract class AbsActionDialogFragment : ViewFragment() {
    data class ItemBean(val tag:Any, val text:String, val drawRes:Int, val imageTintList: ColorStateList? = null)

    abstract val items: List<ItemBean>

    abstract fun notify(tag: Any)

    fun itemHeight(fragment: Fragment) : Float {
        return fragment.resources.getDimension(com.au.module_androidui.R.dimen.action_dialog_item_height)
    }

    override fun onUiCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return LinearLayout(inflater.context).also {
            root = it
            it.orientation = LinearLayout.VERTICAL
            it.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

            var index = 0
            val itemHeight = itemHeight(this).toInt()
            for (item in items) {
                index++
                val v = createFlowItem(item)
                it.addView(v, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, itemHeight))
            }

            it.addView(Space(inflater.context), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, itemHeight / 4))
        }
    }

    private fun dismiss() {
        parentFragment.asOrNull<DialogFragment>()?.dismiss()
    }

    private fun createFlowItem(bean: ItemBean): View {
        return LeftIconAndTextBinding.inflate(requireActivity().layoutInflater, null, false)
            .also {
                it.tv.text = bean.text
                it.root.tag = bean.tag
                it.icon.setImageResource(bean.drawRes)
                if(bean.imageTintList != null) it.icon.imageTintList = bean.imageTintList
                it.root.onClick { v->
                    notify(v.tag)
                    dismiss()
                }
            }.root
    }
}