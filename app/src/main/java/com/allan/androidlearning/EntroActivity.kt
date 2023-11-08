package com.allan.androidlearning

import android.os.Bundle
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.allan.androidlearning.activities.DataStoreFragment
import com.allan.androidlearning.activities.DialogsFragment
import com.allan.androidlearning.activities.FontTestFragment
import com.allan.androidlearning.activities.LiveDataFragment
import com.allan.androidlearning.activities.WebBridgeFragment
import com.allan.androidlearning.databinding.ActivityEntroBinding
import com.au.module_android.ui.bindings.BindingActivity
import com.au.module_android.ui.FragmentRootActivity
import com.au.module_android.click.onClick
import com.au.module_android.utils.unsafeLazy
import com.google.android.material.button.MaterialButton

class EntroActivity : BindingActivity<ActivityEntroBinding>() {

    private val allFragments:List<Class<out Fragment>> by unsafeLazy {
        listOf(FontTestFragment::class.java,
            WebBridgeFragment::class.java,
            DataStoreFragment::class.java,
            LiveDataFragment::class.java,
            DialogsFragment::class.java,)
    }

    override fun afterViewCreated(savedInstanceState: Bundle?, viewBinding: ActivityEntroBinding) {
        setSupportActionBar(findViewById(R.id.toolbar))
        allFragments.forEach {fragmentClass->
            val btn = MaterialButton(this)
            btn.text = fragmentClass.simpleName.replace("Fragment", "")
            btn.onClick {
                FragmentRootActivity.start(this, fragmentClass)
            }
            viewBinding.buttonsHost.addView(btn, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }
        viewBinding.buttonsHost
    }
}
