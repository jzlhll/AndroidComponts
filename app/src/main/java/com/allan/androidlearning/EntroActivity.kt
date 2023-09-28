package com.allan.androidlearning

import android.content.res.Resources
import android.os.Bundle
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.allan.androidlearning.activities.FontTestFragment
import com.allan.androidlearning.activities.LiveDataFragment
import com.allan.androidlearning.databinding.ActivityEntroBinding
import com.au.module_android.arct.BaseBindingActivity
import com.au.module_android.base.FragmentRootActivity
import com.au.module_android.click.onClick
import com.au.module_android.utils.unsafeLazy
import com.google.android.material.button.MaterialButton

class EntroActivity : BaseBindingActivity<ActivityEntroBinding>() {

    private val allFragments:List<Class<out Fragment>> by unsafeLazy {
        listOf(FontTestFragment::class.java,
            LiveDataFragment::class.java)
    }

    override fun onCommonAfterCreateView(
        owner: LifecycleOwner,
        savedInstanceState: Bundle?,
        resources: Resources
    ) {
        setSupportActionBar(findViewById(R.id.toolbar))
        allFragments.forEach {fragmentClass->
            val btn = MaterialButton(this)
            btn.text = fragmentClass.simpleName.replace("Fragment", "Frg")
            btn.onClick {
                FragmentRootActivity.startFragmentActivity(this, fragmentClass)
            }
            binding.buttonsHost.addView(btn, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }
        binding.buttonsHost
    }

}
