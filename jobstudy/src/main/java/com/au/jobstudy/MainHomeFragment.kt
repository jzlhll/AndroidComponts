package com.au.jobstudy

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.allan.nested.decoration.PaddingItemDecoration
import com.au.jobstudy.check.CheckConsts
import com.au.jobstudy.check.NameList
import com.au.jobstudy.check.StarList
import com.au.jobstudy.check.StatusMode
import com.au.jobstudy.checkwith.CheckWithFragment
import com.au.jobstudy.utils.Dayer
import com.au.jobstudy.utils.WeekDateUtil
import com.au.jobstudy.utils.WeekDateUtil.currentTimeToHelloGood
import com.au.jobstudy.databinding.FragmentMainHomeBinding
import com.au.jobstudy.home.HomeRcvAdapter
import com.au.jobstudy.home.HomeRcvBean
import com.au.jobstudy.home.HomeRcvHeadBean
import com.au.jobstudy.home.HomeRcvItemBean
import com.au.jobstudy.home.HomeRcvTitleBean
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.dp
import kotlinx.coroutines.launch
import java.util.jar.Attributes.Name

class MainHomeFragment : BindingFragment<FragmentMainHomeBinding>() {
    private lateinit var adapter: HomeRcvAdapter
    private val userName = NameList.NAMES_JIANG_TJ

    private val itemClick : (HomeRcvItemBean)->Unit = { itemBean->
        CheckWithFragment.start(requireContext(), itemBean.oneWork)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.refresher.apply {
            initEarlyAsFake(binding.root)
        }

        binding.rcv.layoutManager = LinearLayoutManager(binding.rcv.context)
        binding.rcv.addItemDecoration(
            PaddingItemDecoration(
            0,
            4.dp,
            true)
        )

        HomeRcvAdapter.click = itemClick
        binding.rcv.adapter = HomeRcvAdapter().also { adapter = it }

        CheckConsts.statusChangedLiveData.observe(viewLifecycleOwner) {
            val list = mutableListOf<HomeRcvBean>()
            list.add(HomeRcvHeadBean(NameList.NAMES_JIANG_TJ, NameList.HUAZHONG_SCROLL))
            val uncompletedWorks = CheckConsts.todayUncompletedWorks()
            if (uncompletedWorks.isEmpty()) {
                list.add(HomeRcvTitleBean("今天的任务已经全部完成，棒棒的！", 1))
            } else {
                list.add(HomeRcvTitleBean("今天的任务：", 1))
                uncompletedWorks.forEach {
                    list.add(HomeRcvItemBean(it))
                }
            }

            val uncompletedWorksYesterday = CheckConsts.yesterdayUncompletedWorks()
            if (uncompletedWorksYesterday.isEmpty()) {
                list.add(HomeRcvTitleBean("昨天的任务已经全部完成，棒棒的！", 2))
            } else {
                list.add(HomeRcvTitleBean("昨天还有剩余的任务没有完成：", 2))
                uncompletedWorksYesterday.forEach {
                    list.add(HomeRcvItemBean(it))
                }
            }

            val uncompletedWorksWeekly = CheckConsts.weeklyUncompletedWorks()
            if (uncompletedWorksWeekly.isEmpty()) {
                list.add(HomeRcvTitleBean("每周任务已经全部完成，棒棒的！", 3))
            } else {
                val todayIndex = WeekDateUtil.getTodayWeekIndex()
                val leftDay = 7 - todayIndex
                list.add(HomeRcvTitleBean("每周任务 (剩余完成时间${leftDay}天)：", 3))
                uncompletedWorksWeekly.forEach {
                    list.add(HomeRcvItemBean(it))
                }
            }

            list.add(HomeRcvBean.empty)
            adapter.submitList(list, false)
        }
    }

    override fun onResume() {
        super.onResume()

        binding.weather.text = WeekDateUtil.getTodayWeekN()

        val time = String.format(getString(R.string.name_hello_format), userName, currentTimeToHelloGood())
        if (binding.title.text != time) {
            binding.title.text = time
        }
    }
}