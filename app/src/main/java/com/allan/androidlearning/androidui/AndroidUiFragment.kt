package com.allan.androidlearning.androidui

import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.allan.androidlearning.databinding.FragmentAndroidUiBinding
import com.allan.classnameanno.EntroFrgName
import com.allan.nested.viewpager2.simplePagerAdapter
import com.au.module_android.utils.asOrNull
import com.au.module_android.widget.CustomFontText
import com.au.module_android.widget.FontMode
import com.au.module_android.ui.bindings.BindingFragment
import com.google.android.material.tabs.TabLayout

/**
 * @author allan
 * @date :2024/8/19 15:15
 * @description:
 */
@EntroFrgName
class AndroidUiFragment : BindingFragment<FragmentAndroidUiBinding>() {
    override fun hasToolbar(): Boolean {
        return true
    }

    private val pages = listOf(
        Pair("Components", AndroidUi1Fragment::class.java),
        Pair("Action", AndroidUi2Fragment::class.java),
        Pair("Edit", AndroidUi3Fragment::class.java),
    )

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        //不经过post 有毛用 binding.viewPager.overScrollNever()
        binding.viewPager.simplePagerAdapter(this, pages) { _, pair ->
            pair.second.getDeclaredConstructor().newInstance()
        }

        binding.tabLayout.initAttachToViewPage2AsCustomFontText(binding.viewPager, pages)
    }

}