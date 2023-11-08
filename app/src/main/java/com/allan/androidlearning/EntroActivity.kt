package com.allan.androidlearning

import android.os.Bundle
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.allan.androidlearning.activities.FontTestFragment
import com.allan.androidlearning.activities.LiveDataFragment
import com.allan.androidlearning.activities.WebBridgeFragment
import com.allan.androidlearning.databinding.ActivityEntroBinding
import com.au.module_android.ui.AbsBindingActivity
import com.au.module_android.base.FragmentRootActivity
import com.au.module_android.click.onClick
import com.au.module_android.utils.unsafeLazy
import com.google.android.material.button.MaterialButton

class EntroActivity : AbsBindingActivity<ActivityEntroBinding>() {

    private val allFragments:List<Class<out Fragment>> by unsafeLazy {
        listOf(FontTestFragment::class.java,
            WebBridgeFragment::class.java,
            LiveDataFragment::class.java)
    }

    override fun afterViewCreated(savedInstanceState: Bundle?, viewBinding: ActivityEntroBinding) {
        setSupportActionBar(findViewById(R.id.toolbar))
        allFragments.forEach {fragmentClass->
            val btn = MaterialButton(this)
            btn.text = fragmentClass.simpleName.replace("Fragment", "Frg")
            btn.onClick {
                FragmentRootActivity.start(this, fragmentClass)
            }
            viewBinding.buttonsHost.addView(btn, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }
        viewBinding.buttonsHost
    }
}
