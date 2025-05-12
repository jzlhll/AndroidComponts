package com.allan.androidlearning.activities2

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.allan.classnameanno.EntryFrgName
import com.au.logsystem.LogSystemFragment
import com.au.module_android.click.onClick
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.ui.views.ViewFragment
import com.au.module_android.utils.dp
import com.au.module_android.widget.CustomButton

@EntryFrgName(priority = 11)
class LogSystemFragment : ViewFragment() {
    override fun onUiCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FrameLayout(inflater.context).apply {
            addView(CustomButton(inflater.context).also {
                it.text = "Log System"
                it.gravity = Gravity.CENTER_HORIZONTAL
                it.layoutParams = FrameLayout.LayoutParams(200.dp, 50.dp).also { lp->
                    lp.topMargin = 20.dp
                }
                it.setBackgroundResource(com.au.module_androidcolor.R.drawable.corner_btn_primary)
                it.onClick {
                    FragmentShellActivity.start(requireActivity(), LogSystemFragment::class.java)
                }
            })
        }
    }
}