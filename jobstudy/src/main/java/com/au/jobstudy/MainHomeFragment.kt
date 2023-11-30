package com.au.jobstudy

import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.allan.nested.layout.SimpleItemsLayout
import com.au.jobstudy.bean.DataItem
import com.au.jobstudy.bean.subjectToColor
import com.au.jobstudy.consts.Dayer
import com.au.jobstudy.databinding.FragmentMainHomeBinding
import com.au.jobstudy.databinding.HomeCheckItemBinding
import com.au.jobstudy.databinding.HomeCheckItemTitleBinding
import com.au.jobstudy.databinding.HomeStarOnlyOneBigBinding
import com.au.jobstudy.databinding.HomeStarThreeStarsBinding
import com.au.jobstudy.home.CheckPointUiData
import com.au.jobstudy.home.ThisWeekUiData
import com.au.jobstudy.util.currentTimeToHelloGood
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.visible
import kotlinx.coroutines.launch

class MainHomeFragment : BindingFragment<FragmentMainHomeBinding>() {
    private lateinit var viewModel:GlobalDataViewModel
    private val userName = "蒋添靖"

    override fun afterViewCreated(savedInstanceState: Bundle?, viewBinding: FragmentMainHomeBinding) {
        binding.mineName.text = userName

        viewModel = ViewModelProvider(requireActivity())[GlobalDataViewModel::class.java]

        binding.checkPointList.itemInflateCreator = object : ((LayoutInflater, SimpleItemsLayout, Boolean, Any)->ViewBinding) {
            override fun invoke(layoutInflate: LayoutInflater, me: SimpleItemsLayout, attachedToParent: Boolean, data: Any): ViewBinding {
                return when (val uiData = data as CheckPointUiData) {
                    is CheckPointUiData.Title -> {
                        HomeCheckItemTitleBinding.inflate(layoutInflate, me, attachedToParent).also {
                            it.workTitle.text = uiData.str
                        }
                    }

                    is CheckPointUiData.Item -> {
                        HomeCheckItemBinding.inflate(layoutInflate, me, attachedToParent).also {
                            it.subjectText.text = uiData.dataItem.subject
                            it.descText.text = uiData.dataItem.desc
                            val color = subjectToColor(uiData.dataItem.subject)
                            it.subjectColor.setBackgroundColor(color)
                        }
                    }
                }
            }

        }

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

        viewModel.busLiveData.observe(this) {bus->
            bus.foreach { key, content ->
                when (key) {
                    "getWeekData" -> {
                        val list = content.real.asOrNull<List<DataItem>>()
                        if (list != null) {
                            val arr = viewModel.dataListToCompletedCount(list)
                            for (a in arr) {
                                binding.thisWeekList.addItem(a)
                            }
                        }
                    }

                    "getDay-Yesterday" -> {
                        val yestDay = content.real.asOrNull<List<DataItem>>()
                        if (yestDay != null) {
                            val yestNoCompleteDay = yestDay.filter { !it.complete }
                            if (yestNoCompleteDay.isNotEmpty()) {
                                binding.checkPointList.addItem(CheckPointUiData.Title("昨天还有未完成的任务~~~"))
                                yestNoCompleteDay.forEach {
                                    binding.checkPointList.addItem(CheckPointUiData.Item(it))
                                }
                            }
                        }
                    }
                    "getDay-Today" -> {
                        val todayData = content.real.asOrNull<List<DataItem>>()
                        if (todayData != null) {
                            val todayNotComplete = todayData.filter { !it.complete }
                            if (todayNotComplete.isNotEmpty()) {
                                binding.checkPointList.addItem(CheckPointUiData.Title("今天的任务："))
                                todayNotComplete.forEach {
                                    binding.checkPointList.addItem(CheckPointUiData.Item(it))
                                }
                            }
                        }
                    }
                }
                true
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val time = String.format(getString(R.string.name_hello_format), userName, currentTimeToHelloGood())

        if (binding.title.text != time) {
            binding.title.text = time
            val curDay = Dayer()
            viewModel.getWeekData(curDay.currentDay)
            viewModel.getDay("" + (curDay.currentDayInt - 1), "getDay-Yesterday")
            viewModel.getDay(curDay.currentDay, "getDay-Today")
        }
    }
}