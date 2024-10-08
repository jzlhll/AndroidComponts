package com.allan.androidlearning.androidui

import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.allan.androidlearning.R
import com.allan.androidlearning.activities.CoroutineFragment
import com.allan.androidlearning.databinding.FragmentAndroidUi2Binding
import com.allan.androidlearning.databinding.FragmentDialogEditBinding
import com.au.module_android.DarkModeAndLocalesConst
import com.au.module_android.click.onClick
import com.au.module_android.ui.FragmentRootActivity
import com.au.module_android.ui.base.IBaseDialog
import com.au.module_android.ui.base.findDialogByContentFragment
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.ui.views.ViewFragment
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.dp
import com.au.module_android.utils.hideImeNew
import com.au.module_android.utils.logd
import com.au.module_android.utils.showImeNew
import com.au.module_android.utils.transparentStatusBar
import com.au.module_androidui.dialogs.ConfirmBottomDialog
import com.au.module_androidui.dialogs.ConfirmCenterDialog
import com.au.module_androidui.dialogs.ConfirmCenterImgDialog
import com.au.module_androidui.dialogs.ConfirmImgBottomDialog
import com.au.module_androidui.dialogs.FragmentBottomSheetDialog
import com.au.module_androidui.toast.ToastBuilder

class AndroidUi2Fragment : BindingFragment<FragmentAndroidUi2Binding>() {
    override fun onBindingCreated(savedInstanceState: Bundle?) {
        darkMode()
        toast()
        selectList()
        dialog()
    }

    fun dialog() {

        binding.androidUiDialog1.onClick {
            ConfirmBottomDialog.show(childFragmentManager, "Title", "This is the content desc for title.",
                "OK", null) {
                it.dismissAllowingStateLoss()
            }
        }
        binding.androidUiDialog2.onClick {
            ConfirmCenterDialog.show(childFragmentManager, "Title", "This is the content desc for title.",
                "OK", null) {
                it.dismissAllowingStateLoss()
            }
        }
        binding.androidUiDialog3.onClick {
            ConfirmCenterImgDialog.show(childFragmentManager,
                R.mipmap.ic_launcher,
                null,
                "Title",
                "This is content and image for title.",
                "Sure",
                null
                ) {
                it.dismissAllowingStateLoss()
            }
        }

        binding.androidUiDialog4.onClick {
            ConfirmImgBottomDialog.show(childFragmentManager,
                R.mipmap.ic_launcher,
                null,
                "Title",
                "This is content and image for title.",
                "Sure",
                null
            ) {
                it.dismissAllowingStateLoss()
            }
        }

        binding.androidUiDialog5.onClick {
            FragmentBottomSheetDialog.show<CoroutineFragment>(childFragmentManager)
        }

        binding.androidUiDialog6.onClick {
            FragmentBottomSheetDialog.show<BottomSheetMaxFragment>(childFragmentManager)
        }

        binding.androidUiDialog7.onClick {
            FragmentBottomSheetDialog.show<EditFragment>(childFragmentManager, hasEditText = true)
        }
    }

    class BottomSheetMaxFragment : ViewFragment() {
        override fun onUiCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            return LinearLayout(inflater.context).also {
                it.orientation = LinearLayout.VERTICAL
                it.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 3000.dp)
                it.setBackgroundColor(Color.GREEN)
            }
        }
    }

    class EditFragment : BindingFragment<FragmentDialogEditBinding>() { //1. 设置显示方向
        private var isInitDialogOnce = false
        override fun onResume() {
            super.onResume()

            if (!isInitDialogOnce) {
                isInitDialogOnce = true
                this.findDialogByContentFragment()?.asOrNull<IBaseDialog>()?.apply {
                    onDismissBlock = { dialog->
                        dialog.window?.let {
                            logd{"hide ime before dismiss"}
                            hideImeNew(it, it.decorView)
                        }
                    }
                }

                binding.closeBtn.onClick {
                    findDialogByContentFragment()?.dismissAllowingStateLoss()
                }
            }

            // 弹出键盘；需要验证是否比较流畅
            findDialogByContentFragment()?.asOrNull<IBaseDialog>()?.let {
                it.window?.let {window->
                    showImeNew(window, binding.etText)
                }
            }
        }
    }

    fun selectList() {
        binding.androidUiSelectList.onClick {
            FragmentRootActivity.start(requireContext(), CoroutineFragment::class.java)
        }
    }

    val getDarkStrFunc = {
        val mode = requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isDark = DarkModeAndLocalesConst.detectDarkMode(requireContext())
        val s2 = when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> "UI_MODE_NIGHT_YES"
            Configuration.UI_MODE_NIGHT_UNDEFINED -> "UI_MODE_NIGHT_UNDEFINED"
            Configuration.UI_MODE_NIGHT_NO -> "UI_MODE_NIGHT_NO"
            else -> "$mode"
        }
        "darkMode\n$s2 : $isDark"
    }

    fun toast() {
        binding.androidUiToast1.onClick {
            ToastBuilder().setMessage("This is a line toast.").setOnTop().toast()
        }

        binding.androidUiToast2.onClick {
            ToastBuilder().setMessage("This is a line toast with icon.").setIcon("success").setOnViewGroup(requireActivity().window.decorView as ViewGroup).toast()
        }

        binding.androidUiToast3.onClick {
            ToastBuilder().setDesc("This is desc for this title.").setMessage("The news for today").setIcon("info").setOnViewGroup(requireActivity().window.decorView as ViewGroup).toast()
        }
    }

    fun darkMode() {
        binding.androidUiDarkmodeTitle.text = getDarkStrFunc()

        binding.androidUiDarkmodeTempingForceLight.onClick {
            requireActivity().transparentStatusBar(false, false)
        }

        binding.androidUiDarkmodeTempingForceDark.onClick {
            requireActivity().transparentStatusBar(true, true)
        }

        binding.androidUiDarkmode1.onClick {
            ToastBuilder().setOnTop().setMessage(getDarkStrFunc()).setIcon("info").toast()
        }
        binding.androidUiSysLocale.onClick {
            val locale = Resources.getSystem().configuration.locales.get(0)
            ToastBuilder().setOnTop().setMessage("locale: " + locale).toast()
        }
        binding.androidUiSysUimode.onClick {
            val uiMode = Resources.getSystem().configuration.uiMode
            ToastBuilder().setOnTop().setMessage("uiMode: $uiMode").toast()
        }
    }
}