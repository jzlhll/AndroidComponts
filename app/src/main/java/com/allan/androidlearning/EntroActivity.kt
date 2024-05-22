package com.allan.androidlearning

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.allan.androidlearning.activities.CanvasFragment
import com.allan.androidlearning.activities.DataStoreFragment
import com.allan.androidlearning.activities.DialogsFragment
import com.allan.androidlearning.activities.FontTestFragment
import com.allan.androidlearning.activities.LiveDataFragment
import com.allan.androidlearning.activities.MonoSpaceFragment
import com.allan.androidlearning.activities.WebBridgeFragment
import com.allan.androidlearning.databinding.ActivityEntroBinding
import com.au.module_android.Apps
import com.au.module_android.ui.bindings.BindingActivity
import com.au.module_android.ui.FragmentRootActivity
import com.au.module_android.click.onClick
import com.au.module_android.utils.unsafeLazy
import com.au.module_androiduilight.toast.toastOnTop
import com.google.android.material.button.MaterialButton

class EntroActivity : BindingActivity<ActivityEntroBinding>() {
    private val allFragments:List<Class<out Fragment>> by unsafeLazy {
        listOf(FontTestFragment::class.java,
            WebBridgeFragment::class.java,
            DataStoreFragment::class.java,
            LiveDataFragment::class.java,
            DialogsFragment::class.java,
            CanvasFragment::class.java,
            MonoSpaceFragment::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(findViewById(R.id.toolbar))
        allFragments.forEach {fragmentClass->
            val btn = MaterialButton(this)
            btn.text = fragmentClass.simpleName.replace("Fragment", "")
            btn.onClick {
                FragmentRootActivity.start(this, fragmentClass)
            }
            binding.buttonsHost.addView(btn, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }
        binding.buttonsHost.addView(MaterialButton(this).also {
                                                              it.text = "success"
            it.onClick {
                NetworkHelperToastMgr.toastSuccess()
            }
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        binding.buttonsHost.addView(MaterialButton(this).also {
            it.text = "fail"
            it.onClick {
                NetworkHelperToastMgr.toastConnectionLost()
            }
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        binding.buttonsHost
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
        Apps.mainHandler.removeCallbacks(toastRun)
        Apps.mainHandler.postDelayed(toastRun, 3000)
    }

    fun toastConnectionLost() {
        val lastTarget = mTargetSuccess
        mTargetSuccess = false
        if (lastTarget != null) {
            Log.d("allan", "delay to toast Target false")
            mTargetSuccess = false //不得随意调整位置
            Apps.mainHandler.removeCallbacks(toastRun)
            Apps.mainHandler.postDelayed(toastRun, 3000)
        } else {
            Log.d("allan", "delay to toast Target false no toast")
        }
    }
}
