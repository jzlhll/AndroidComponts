package com.au.jobstudy

import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.allan.nested.decoration.PaddingItemDecoration
import com.allan.nested.layout.SimpleItemsLayout
import com.au.jobstudy.bean.DataItem
import com.au.jobstudy.consts.Dayer
import com.au.jobstudy.consts.WeekDateUtil
import com.au.jobstudy.consts.WeekDateUtil.currentTimeToHelloGood
import com.au.jobstudy.databinding.FragmentMainHomeBinding
import com.au.jobstudy.databinding.HomeStarOnlyOneBigBinding
import com.au.jobstudy.databinding.HomeStarThreeStarsBinding
import com.au.jobstudy.home.HomeRcvAdapter
import com.au.jobstudy.home.ThisWeekUiData
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.dp
import com.au.module_android.utils.visible

class MainHomeFragment : BindingFragment<FragmentMainHomeBinding>() {
    private lateinit var adapter: HomeRcvAdapter
    private val userName = "蒋添靖"
    private val scroll = "华中师范宝安附属学校"

    override fun afterViewCreated(savedInstanceState: Bundle?, viewBinding: FragmentMainHomeBinding) {
        binding.root.refresher.apply {
            initEarlyAsFake(binding.root)
        }

        binding.mineName.text = userName
        binding.mineScholl.text = scroll

        binding.rcv.layoutManager = LinearLayoutManager(binding.rcv.context)
        binding.rcv.addItemDecoration(
            PaddingItemDecoration(
            0,
            8.dp,
            true)
        )

        HomeRcvAdapter.click = {

        }

        binding.rcv.adapter = HomeRcvAdapter().also { adapter = it }

        binding.thisWeekList.itemInflateCreator = object : ((LayoutInflater, SimpleItemsLayout, Boolean, Any)->ViewBinding) {
            override fun invoke(layoutInflate: LayoutInflater, me: SimpleItemsLayout, attachedToParent: Boolean, data: Any): ViewBinding {
                return when (val uiData = data as ThisWeekUiData) {
                    is ThisWeekUiData.ThisWeekLayoutData -> {
                        HomeStarOnlyOneBigBinding.inflate(layoutInflate, me, attachedToParent).also {
                            it.numbersTv.text = "${uiData.num}"
                        }
                    }

                    is ThisWeekUiData.ThisWeekEachLayoutData -> {
                        HomeStarThreeStarsBinding.inflate(layoutInflate, me, attachedToParent).also { vb->
                            var count = 0
                            uiData.eachStars.forEach {
                                count++
                                when (count) {
                                    1 -> {
                                        vb.name1.text = it.first.name
                                        vb.value1.text = "${it.second}"
                                        vb.name1.visible()
                                        vb.value1.visible()
                                        vb.pic1.visible()
                                    }
                                    2 -> {
                                        vb.name2.text = it.first.name
                                        vb.value2.text = "${it.second}"
                                        vb.name2.visible()
                                        vb.value2.visible()
                                        vb.pic2.visible()
                                    }
                                    3 -> {
                                        vb.name3.text = it.first.name
                                        vb.value3.text = "${it.second}"
                                        vb.name3.visible()
                                        vb.value3.visible()
                                        vb.pic3.visible()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        GlobalDataViewModel.busLiveData.observe(this) {bus->
            bus.foreach { key, content ->
                if (key.startsWith("getWeekData-")) {
                    val list = content.real.asOrNull<List<DataItem>>()
                    if (list != null) {
                        val arr = GlobalDataViewModel.dataListToCompletedCount(list)
                        for (a in arr) {
                            binding.thisWeekList.addItem(a)
                        }
                    }

                    val currentDay = key.replace("getWeekData-", "")

                    GlobalDataViewModel.getDay(WeekDateUtil.getYesterday(currentDay), "getDay-Yesterday")
                    GlobalDataViewModel.getDay(currentDay, "getDay-Today")
                    true
                } else {
                    false
                }
            }

            val shouldWork = GlobalDataViewModel.isBusGetTodayAndYesterday(bus)
            if (shouldWork) {
                adapter.submitList(GlobalDataViewModel.busToAdapterData(bus), false)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val time = String.format(getString(R.string.name_hello_format), userName, currentTimeToHelloGood())

        if (binding.title.text != time) {
            binding.title.text = time
            val curDay = Dayer()
            GlobalDataViewModel.getWeekData(curDay.currentDay, true, "getWeekData-" + curDay.currentDay)
        }
    }
}