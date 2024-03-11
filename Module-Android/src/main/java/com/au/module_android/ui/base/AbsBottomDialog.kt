package com.au.module_android.ui.base

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.au.module.android.R
import com.au.module_android.screenadapter.ToutiaoScreenAdapter
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.transparentStatusBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

@Deprecated("基础框架的一环，请使用BindingXXXDialog或者ViewXXXDialog")
abstract class AbsBottomDialog<D:IBaseDialog>(private val hasEditText:Boolean)
        : BottomSheetDialogFragment(), IBottomDialog<D> {
    /**
     * 尽量早一点调用。在show之前。如果是继承，则放在init{}
     */
    override var onDismissBlock:((D)->Unit)? = null

    /**
     * 尽量早一点调用。在show之前。如果是继承，则放在init{}
     */
    override var onShownBlock:((D)->Unit)? = null

    override var rootView: View? = null

    /**
     * 对话框的window
     */
    override val window: Window?
        get() = dialog?.window

    override var createdDialog : Dialog? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sheetDialog = BeforeDismissBottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme).also {
            ToutiaoScreenAdapter.attach(it)
        }
        createdDialog = sheetDialog
        sheetDialog.behavior.skipCollapsed = true
        sheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        sheetDialog.beforeDismissCallback = object : BeforeDismissBottomSheetDialog.BeforeDismissCallback {
            override fun onBeforeDismiss(dialog: BeforeDismissBottomSheetDialog?) {
                onDismissBlock?.invoke(this as D)
                onDismissBlock = null
            }
        }

        sheetDialog.setOnShowListener {
            onShownBlock?.invoke(this as D)
            onShownBlock = null
        }
        return sheetDialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return onCreatingView(inflater, container, savedInstanceState).also {
            rootView = it
        }
    }

    override fun onStart() {
        super.onStart()

        //抬起底部间距, 让navBar与布局一直颜色
        // warn1: 不能忽略dialog?window的条件，否则就变成了处理activity，而不是本dialog的window
        // warn2: 设置了transparentStatusBar后，键盘无法弹起。
        if (!hasEditText) {
            dialog?.window?.let { window ->
                transparentStatusBar(window) { _, _, navigationBarHeight ->
                    view?.updatePadding(bottom = navigationBarHeight)
                    WindowInsetsCompat.CONSUMED
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        createdDialog?.let { d->
            d.asOrNull<BeforeDismissBottomSheetDialog>()?.beforeDismissCallback = null
            d.setOnShowListener(null)
        }
    }

    override fun findToastViewGroup(): ViewGroup? {
        //bottomSheetDialog的内置逻辑。
        rootView?.let { tdv->
            val design_bottom_sheet = tdv.parent.asOrNull<ViewGroup>()
            design_bottom_sheet?.let { dbs->
                return dbs.parent.asOrNull<ViewGroup>() //coordinator
            }
        }
        return null
    }
}


open class BeforeDismissBottomSheetDialog : BottomSheetDialog {
    interface BeforeDismissCallback {
        fun onBeforeDismiss(dialog: BeforeDismissBottomSheetDialog?)
    }

    var beforeDismissCallback: BeforeDismissCallback? = null
    constructor(context: Context) : super(context)
    constructor(context: Context, theme: Int) : super(context, theme)
    protected constructor(context: Context, cancelable: Boolean, cancelListener: DialogInterface.OnCancelListener?) : super(context, cancelable, cancelListener)

    override fun dismiss() {
        beforeDismissCallback?.onBeforeDismiss(this)
        super.dismiss()
    }
}
