package com.au.jobstudy

import android.os.Bundle
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.allan.nested.bottom_nav.BottomNavAdapter
import com.allan.nested.bottom_nav.BottomPageBean
import com.allan.nested.recyclerview.NoScrollLinearLayoutManager
import com.allan.nested.viewpager2.simplePagerAdapter
import com.au.jobstudy.databinding.ActivityMainBinding
import com.au.jobstudy.databinding.BottomPageMenuBinding
import com.au.module_android.Globals.app
import com.au.module_android.ui.bindings.BindingActivity
import com.au.module_android.utils.transparentStatusBar
import com.au.module_android.utils.unsafeLazy

class MainActivity : BindingActivity<ActivityMainBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparentStatusBar(this, isBlackStatusBarTextColor = true, isBlackNavigationBarTextColor = true, ::onStatusBar)
    }

    private fun onStatusBar(
        insets: WindowInsetsCompat,
        statusBarsHeight: Int,
        navigationBarHeight: Int
    ): WindowInsetsCompat {
        binding.root.updatePadding(top = statusBarsHeight, bottom = navigationBarHeight)
        return insets
    }

    private val colorTextGray by unsafeLazy { app.getColor(com.au.module_androidex.R.color.color_999999) }
    private val colorPrimary by unsafeLazy { app.getColor(R.color.color_primary) }

    private val bottomNavList = listOf(
        BottomPageBean(true, R.string.bottom_home, R.drawable.ic_bottom_home, R.drawable.ic_bottom_home_select,
            colorTextGray,
            colorPrimary),
        BottomPageBean(false, R.string.bottom_boardcast, R.drawable.ic_bottom_boardcast, R.drawable.ic_bottom_boardcast_sel,
            colorTextGray,
            colorPrimary),
        BottomPageBean(false, R.string.bottom_mine, R.drawable.ic_bottom_mine, R.drawable.ic_bottom_mine_select,
            colorTextGray,
            colorPrimary)
    )

    private val pages = listOf(
        MainHomeFragment::class.java, MainFriendsFragment::class.java, MainMineFragment::class.java
    )

    override fun afterViewCreated(savedInstanceState: Bundle?, viewBinding: ActivityMainBinding) {
        viewBinding.mainViewPager.offscreenPageLimit = 1
        viewBinding.mainViewPager.simplePagerAdapter(this, pages) { _, fragment ->
            fragment.getDeclaredConstructor().newInstance()
        }

        viewBinding.mainViewPager.isUserInputEnabled = false

        viewBinding.mainBottomNavRcv.layoutManager = NoScrollLinearLayoutManager(this).also { it.orientation = RecyclerView.HORIZONTAL }
        viewBinding.mainBottomNavRcv.adapter = BottomNavAdapter<BottomPageMenuBinding>(bottomNavList, BottomPageMenuBinding::class.java).also { adapter->
            adapter.bindWithBottomNav(viewBinding.mainViewPager,
                itemViewChangeFun = { viewBinding, bean->
                viewBinding.titleTv.setText(bean.titleRes)
                if (bean.isSelected) {
                    viewBinding.ivLogo.setImageResource(bean.selectIconRes)
                    viewBinding.titleTv.setTextColor(bean.selectTitleColor)
                } else {
                    viewBinding.ivLogo.setImageResource(bean.iconRes)
                    viewBinding.titleTv.setTextColor(bean.titleColor)
                }
            },
                switchBtnAnimObjectApply = {
                it.ivLogo
            })
        }
    }
}