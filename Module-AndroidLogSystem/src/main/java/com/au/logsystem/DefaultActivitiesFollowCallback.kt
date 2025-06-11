package com.au.logsystem

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import com.au.logsystem.oncelog.OnceLogViewFragment
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.utils.dp

class DefaultActivitiesFollowCallback : AllActivityFollowCallback() {
    override val layoutRes: Int
        get() = R.layout.log_system_float_btn

    override val rootId: Int
        get() = R.id.dragRoot

    override fun showParams(): FrameLayout.LayoutParams {
        return FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            setMargins(0.dp, 0.dp, 20.dp, 24.dp)
        }
    }

    override fun canShow(activity: Activity, savedInstanceState: Bundle?): Boolean {
        if (activity is FragmentShellActivity) {
            val cls = activity.fragmentClass
            if (cls == LogSystemFragment::class.java || cls == OnceLogViewFragment::class.java) {
                return false
            }
            return true
        }
        return false
    }
}