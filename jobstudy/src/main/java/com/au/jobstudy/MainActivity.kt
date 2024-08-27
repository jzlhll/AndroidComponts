package com.au.jobstudy

import android.graphics.Color
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.allan.nested.bottom_nav.BottomNavAdapter
import com.allan.nested.bottom_nav.BottomPageBean
import com.allan.nested.recyclerview.NoScrollLinearLayoutManager
import com.allan.nested.viewpager2.simplePagerAdapter
import com.au.jobstudy.databinding.ActivityMainBinding
import com.au.jobstudy.databinding.BottomPageMenuBinding
import com.au.module_android.Globals.app
import com.au.module_android.ui.bindings.BindingActivity
import com.au.module_android.utils.unsafeLazy

class MainActivity : BindingActivity<ActivityMainBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.mainViewPager.offscreenPageLimit = 1
        binding.mainViewPager.simplePagerAdapter(this, pages) { _, fragment ->
            fragment.getDeclaredConstructor().newInstance()
        }

        binding.mainViewPager.isUserInputEnabled = false

        binding.mainBottomNavRcv.layoutManager = NoScrollLinearLayoutManager(this).also { it.orientation = RecyclerView.HORIZONTAL }
        binding.mainBottomNavRcv.adapter = BottomNavAdapter<BottomPageMenuBinding>(bottomNavList, BottomPageMenuBinding::class.java).also { adapter->
            adapter.bindWithBottomNav(binding.mainViewPager,
                itemViewChangeFun = { binding, bean->
                    binding.titleTv.setText(bean.titleRes)
                    if (bean.isSelected) {
                        binding.ivLogo.setImageResource(bean.selectIconRes)
                        binding.titleTv.setTextColor(bean.selectTitleColor)
                    } else {
                        binding.ivLogo.setImageResource(bean.iconRes)
                        binding.titleTv.setTextColor(bean.titleColor)
                    }
                },
                switchBtnAnimObjectApply = {
                    it.ivLogo
                })
        }
    }

    private val colorTextGray by unsafeLazy { Color.parseColor("#999999") }
    private val colorPrimary by unsafeLazy { app.getColor(R.color.colorPrimary) }

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
        MainHomeFragment::class.java, MainStarsFragment::class.java, MainMineFragment::class.java
    )
}