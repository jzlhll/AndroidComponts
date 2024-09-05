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
import com.au.module_android.screenadapter.ToutiaoScreenAdapter
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.transparentStatusBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class AbsBottomDialog(private val hasEditText:Boolean)
    : BottomSheetDialogFragment(), IBaseDialog {
    /**
     * 尽量早一点调用。在show之前。如果是继承，则放在init{}
     */
    override var onDismissBlock:((IBaseDialog)->Unit)? = null

    /**
     * 尽量早一点调用。在show之前。如果是继承，则放在init{}
     */
    override var onShownBlock:((IBaseDialog)->Unit)? = null

    override var rootView: View? = null

    private var _onInitUiBlock:((rootView:View)->Unit)? = null

    /**
     * 尽量早一点调用。构建dialog对象的时候就调用。
     * 只用作初始化一些界面。
     */
    open fun setOnInitUiBlock(block:((rootView:View)->Unit)) {
        _onInitUiBlock = block
    }

    /**
     * 对话框的window
     */
    override val window: Window?
        get() = dialog?.window

    override var createdDialog : Dialog? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sheetDialog = BeforeDismissBottomSheetDialog(requireContext(), com.au.module_androidcolor.R.style.StyleBottomSheetDialogTheme).also {
            ToutiaoScreenAdapter.attach(it)
        }
        createdDialog = sheetDialog
        sheetDialog.behavior.skipCollapsed = true
        sheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        sheetDialog.beforeDismissCallback = object : BeforeDismissBottomSheetDialog.BeforeDismissCallback {
            override fun onBeforeDismiss(dialog: BeforeDismissBottomSheetDialog?) {
                onDismissBlock?.invoke(this@AbsBottomDialog)
                onDismissBlock = null
            }
        }

        sheetDialog.setOnShowListener {
            onShownBlock?.invoke(this@AbsBottomDialog)
            onShownBlock = null
        }
        return sheetDialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return onCreatingView(inflater, container, savedInstanceState).also {
            rootView = it
            _onInitUiBlock?.invoke(it)
            _onInitUiBlock = null
        }
    }

    override fun onStart() {
        super.onStart()

        //抬起底部间距, 让navBar与布局颜色一致
        // warn1: 不能忽略dialog?window的条件，否则就变成了处理activity，而不是本dialog的window
        // warn2: 设置了transparentStatusBar后，键盘无法弹起。
        // warn3: 那么，布局就从底部透到nav之下，从根部开始显示。
        if (!hasEditText) { //todo android15 check
            dialog?.window?.apply {
                transparentStatusBar { _, _, navigationBarHeight ->
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

    /**
     * 因为当!hasEditText，我们已经updatePadding，那么，相当于压缩了View的大小。
     * 因此传入的height需要考虑将高度增加。
     */
    val isPaddingNavigationBarHeight:Boolean
        get() = !hasEditText

    override fun findToastViewGroup(): ViewGroup? {
        //bottomSheetDialog的内置逻辑。
        rootView?.let { tdv->
            val design_bottom_sheet = tdv.parent.asOrNull<ViewGroup>()
            design_bottom_sheet?.let { dbs->
                val coordinator = dbs.parent.asOrNull<ViewGroup>() //coordinator CoordinatorLayout
                return coordinator?.parent.asOrNull() //container FrameLayout
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
