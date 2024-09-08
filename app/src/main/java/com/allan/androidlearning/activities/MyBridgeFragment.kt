package com.allan.androidlearning.activities

import android.graphics.Color
import android.widget.FrameLayout
import com.au.module_android.Globals.getColor
import com.au.module_android.ui.base.IFullWindow
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.transparentStatusBar
import com.github.lzyzsd.jsbridge.BridgeWebViewExFragment

open class MyBridgeFragment : BridgeWebViewExFragment(), IFullWindow {
    open fun isDialog() = false

    open fun forceLight() = false

    override fun webViewBackgroundColor(): Int {
        requireActivity().transparentStatusBar(true,  true) { insets, statusHeight, navHeight->
            webView.layoutParams = webView.layoutParams.asOrNull<FrameLayout.LayoutParams>()?.also {
                it.setMargins(0, statusHeight, 0, navHeight)
            }
            fullVideoLayout.layoutParams = fullVideoLayout.layoutParams.asOrNull<FrameLayout.LayoutParams>()?.also {
                it.setMargins(0, statusHeight, 0, navHeight)
            }
            insets
        }

        return if (forceLight()) {
            Color.WHITE
        } else if (isDialog()) {
            getColor(com.au.module_androidcolor.R.color.windowDialogBackground)
        } else {
            getColor(com.au.module_androidcolor.R.color.windowBackground)
        }
    }

    override fun isPaddingNavBar(): Boolean {
        return false
    }

    override fun isPaddingStatusBar(): Boolean {
        return false
    }
}