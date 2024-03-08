package com.au.module_android.ui.base

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.au.module.android.R

interface IDialog<D:IBaseDialog> : IBottomDialog<D> {

    /**
     * 尽量早一点调用。在show之前。如果是继承，则放在init{}
     */
    var onDialogViewLayoutParamBlock:((contentViewGroup: View, newLp: FrameLayout.LayoutParams)->Unit)?
}


/**
 * @author allan
 * @date :2024/2/19 17:45
 * @description:
 */
enum class DialogMode {
    Center,
    Bottom,
    Top
}

fun DialogMode.toAnimRes():Int {
    return when (this) {
        DialogMode.Center -> R.style.AnimScaleCenter
        DialogMode.Bottom -> R.style.PopupWindowBottomIn
        DialogMode.Top ->R.style.PopupWindowTopIn
    }
}

fun DialogMode.toGravity():Int {
    return when (this) {
        DialogMode.Center -> Gravity.CENTER
        DialogMode.Bottom -> Gravity.BOTTOM
        DialogMode.Top -> Gravity.TOP
    }
}

/**
 * 有的时候，我们把fragment显示到了XXXDialog中，通过该方法来找到对应的dialog。
 * 这样就可继而调用dismiss等函数。
 * @return 找到的dialog
 * @param contentFragment 显示的contentFragment
 */
fun findDialog(contentFragment: Fragment?): DialogFragment? {
    contentFragment ?: return null
    if (contentFragment is DialogFragment) {
        return contentFragment
    }
    return findDialog(contentFragment.parentFragment)
}