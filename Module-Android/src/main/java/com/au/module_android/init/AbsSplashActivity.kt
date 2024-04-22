package com.au.module_android.init

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.au.module_android.Globals
import android.content.Intent
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

private var lastLauncherApp = 0L

/**
 * @author au
 * @date :2023/11/7 9:33
 * @description:
 */
abstract class AbsSplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(createLayout())
        splashScreen.setKeepOnScreenCondition { true }
        launcherApp(intent)
    }

    /**
     * 创建基础界面。无需设置图标。通过主题搞定的。
     */
    open fun createLayout(): ViewGroup {
        val layout = RelativeLayout(this)
        layout.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        return layout
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        launcherApp(intent)
    }

    abstract fun goActivity()

    fun launcherApp(intent: Intent?) {
        //直接从android Studio run起来会初始化多次。
        val cur = System.currentTimeMillis()
        if (cur - lastLauncherApp > 1000L) {
            lastLauncherApp = cur
        } else {
            return
        }

        android.util.Log.d("SplashActivity", "launch app")
        Globals.activityList.forEach {
            if (it != this) {
                it.finish()
            }
        }

        goActivity()

        this.finish()
    }
}