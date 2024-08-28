package com.allan.androidlearning.androidui

import android.os.Bundle
import com.allan.androidlearning.databinding.FragmentAndroidUiBinding
import com.allan.classnameanno.EntroFrgName
import com.allan.nested.viewpager2.simplePagerAdapter
import com.au.module_android.ui.bindings.BindingFragment

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
        Pair("Components2", AndroidUi3Fragment::class.java),
        Pair("Action", AndroidUi2Fragment::class.java),
    )

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        //不经过post 有毛用 binding.viewPager.overScrollNever()
        binding.viewPager.simplePagerAdapter(this, pages) { _, pair ->
            pair.second.getDeclaredConstructor().newInstance()
        }

        binding.tabLayout.initAttachToViewPage2AsCustomFontText(binding.viewPager, pages)
    }

}