package com.allan.androidlearning.androidui

import android.os.Bundle
import android.view.View
import com.allan.androidlearning.R
import com.allan.androidlearning.databinding.FragmentAndroidUiBinding
import com.allan.classnameanno.EntryFrgName
import com.allan.nested.viewpager2.simplePagerAdapter
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.ui.views.MenuBean
import com.au.module_android.ui.views.ToolbarInfo

/**
 * @author allan
 * @date :2024/8/19 15:15
 * @description:
 */
@EntryFrgName(priority = 11)
class AndroidUiFragment : BindingFragment<FragmentAndroidUiBinding>() {
    override fun toolbarInfo(): ToolbarInfo {
        return ToolbarInfo(menuBean = MenuBean(R.menu.skip_menu, true) {})
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarManager?.setTitleAlign(false)
    }
}