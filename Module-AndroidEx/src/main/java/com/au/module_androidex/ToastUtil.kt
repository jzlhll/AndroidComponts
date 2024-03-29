package com.au.module_androidex

import androidx.fragment.app.Fragment
import com.au.module_android.toast.ToastBuilder
import com.au.module_android.ui.base.AbsBottomDialog
import com.au.module_android.ui.base.AbsDialog
import com.au.module_android.ui.base.IBaseDialog
import com.au.module_android.ui.base.findDialogByContentFragment

fun ToastBuilder.setOnDialogFragment(dialog: IBaseDialog) : ToastBuilder{
    when (dialog) {
        is AbsDialog -> {
            setDecorView(dialog.findToastViewGroup())
        }

        is AbsBottomDialog -> {
            setDecorView(dialog.findToastViewGroup())
        }

        else -> {
        }
    }
    return this
}

fun ToastBuilder.setOnDialogFragmentByContent(contentFragment: Fragment) : ToastBuilder{
    when (val dialogFragment = findDialogByContentFragment(contentFragment)) {
        is AbsDialog -> {
            setDecorView(dialogFragment.findToastViewGroup())
        }

        is AbsBottomDialog -> {
            setDecorView(dialogFragment.findToastViewGroup())
        }

        else -> {
        }
    }
    return this
}