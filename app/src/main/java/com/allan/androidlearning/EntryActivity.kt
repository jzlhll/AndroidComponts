package com.allan.androidlearning

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Space
import androidx.lifecycle.lifecycleScope
import com.allan.androidlearning.activities.FontTestFragment
import com.allan.androidlearning.activities.LiveDataFragment
import com.allan.androidlearning.activities2.HiltFragment
import com.allan.androidlearning.databinding.ActivityEntryBinding
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.ui.bindings.BindingActivity
import com.au.module_android.utils.getScreenFullSize
import com.au.module_android.utils.logd
import com.au.module_androidui.toast.ToastUtil.toastOnTop
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EntryActivity : BindingActivity<ActivityEntryBinding>() {

    @Inject
    lateinit var mHelper : EntryHelper

    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        logd { "onNewIntent $intent" }
        setIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mHelper.test()

        setSupportActionBar(findViewById(R.id.toolbar))

        val goto = intent?.getStringExtra("goto")
        logd { "goto $goto" }

        val entry = EntryList()
        val entryList = entry.getEntryList().toMutableList()
        entryList.also {
            it.forEach { fragmentClassTriple ->
                val btn = MaterialButton(this)
                btn.text = if(fragmentClassTriple.third != null) fragmentClassTriple.third else fragmentClassTriple.first.simpleName.replace("Fragment", "")
                btn.onClick {
                    FragmentShellActivity.start(this, fragmentClassTriple.first)
                }
                binding.buttonsHost.addView(btn, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
            }

            entry.getAutoEnterClass()?.let{ cls->
                lifecycleScope.launch {
                    delay(500)
                    FragmentShellActivity.start(this@EntryActivity, cls)
                }
            }
        }

        binding.buttonsHost.addView(Space(this), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getScreenFullSize().second / 5))

        if (goto == "LiveData") {
            FragmentShellActivity.start(this, LiveDataFragment::class.java)
        } else if (goto == "FontTest") {
            FragmentShellActivity.start(this, FontTestFragment::class.java)
        }
    }
}

object NetworkHelperToastMgr {
    private var toastRun:Runnable = Runnable {
        val isSuc = mTargetSuccess!! //到了这里必定已经设置过值了
        if (mStableIsSuccess != isSuc) {
            if (isSuc) {
                toastOnTop("wifi suc!")
            } else {
                toastOnTop("wifi fail!")
            }
            Log.d("allan", "发生变化")
        } else {
            Log.d("allan", "变来变去没有发生变化 都是$isSuc 不做toast")
        }
        mStableIsSuccess = isSuc
        Log.d("allan", "最后：最新的Stable $isSuc")
    }

    //我们将要成为的样子，到了delay时间以后，想要的状态
    private var mTargetSuccess:Boolean? = null

    //一段时间内的状态。比如：一直是true，中间闪断false，很快true，则该变量不会发生变化。
    private var mStableIsSuccess:Boolean? = null

    fun toastSuccess() {
        mTargetSuccess = true
        Log.d("allan", "delay to toast Target true")
        Globals.mainHandler.removeCallbacks(toastRun)
        Globals.mainHandler.postDelayed(toastRun, 3000)
    }

    fun toastConnectionLost() {
        val lastTarget = mTargetSuccess
        mTargetSuccess = false
        if (lastTarget != null) {
            Log.d("allan", "delay to toast Target false")
            mTargetSuccess = false //不得随意调整位置
            Globals.mainHandler.removeCallbacks(toastRun)
            Globals.mainHandler.postDelayed(toastRun, 3000)
        } else {
            Log.d("allan", "delay to toast Target false no toast")
        }
    }
}
