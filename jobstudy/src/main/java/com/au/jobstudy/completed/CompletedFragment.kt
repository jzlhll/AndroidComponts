package com.au.jobstudy.completed

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.au.jobstudy.check.CheckConsts
import com.au.jobstudy.check.bean.CompletedEntity
import com.au.jobstudy.checkwith.CheckWithFragment
import com.au.jobstudy.databinding.FragmentCompletedBinding
import com.au.jobstudy.utils.WeekDateUtil
import com.au.module_android.Globals
import com.au.module_android.json.fromJson
import com.au.module_android.permissions.createActivityForResult
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.ui.views.ToolbarInfo
import com.au.module_android.utils.unsafeLazy

class CompletedFragment : BindingFragment<FragmentCompletedBinding>() {
    private lateinit var adpater:CompletedAdapter

    private val viewModel by unsafeLazy { ViewModelProvider(requireActivity())[CompletedViewModel::class] }

    private var loadedLastDay : Int = 0

    private var isInited = false

    private val isWeek by unsafeLazy { arguments?.getBoolean("isWeek") ?: false }

    val activityLauncher = createActivityForResult()

    override fun toolbarInfo() = ToolbarInfo("任务列表")

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.rcv.layoutManager = LinearLayoutManager(requireContext())
        binding.rcv.adapter = CompletedAdapter(itemClick = { bean->
            CheckWithFragment.start(Globals.app, activityLauncher,
                bean.workEntity, bean.completedEntity) {
                val completedEntity = it.data?.getStringExtra("completedEntity")
                if (completedEntity != null) {
                    completedEntity.fromJson<CompletedEntity>()?.dayWorkId?.let { workId->
                        val completedBean = adpater.datas.find { d-> (d is CompletedBean) && d.workEntity.id == workId }
                        if (completedBean != null) {
                            val index = adpater.datas.indexOf(completedBean)
                            viewModel.updateABean(completedBean as CompletedBean) {
                                adpater.notifyItemChanged(index)
                            }
                        }
                    }
                }
            }
        }).also {
            adpater = it
            it.loadMoreAction = {
                if (!isWeek) {
                    viewModel.fetch(getNewDays(loadedLastDay))
                } else {
                    viewModel.fetchWeek(getWeeks(loadedLastDay))
                }
            }
        }

        viewModel.completedBeans.observe(viewLifecycleOwner) {
            binding.loading.hide()
            val isEmpty = it.isEmpty()
            if (!isInited) {
                adpater.initDatas(it, true)
                isInited = true
            } else {
                if (!isEmpty) {
                    adpater.appendDatas(it, true)
                } else {
                    adpater.setNoMore()
                }
            }
        }

        if (!isWeek) {
            val days = getNewDays(CheckConsts.currentDay())
            viewModel.fetch(days)

        } else {
            val weeks = getWeeks(CheckConsts.dayer!!.weekStartDay)
            viewModel.fetchWeek(weeks)
        }

    }

    private fun getWeeks(weekStartDay:Int) : IntArray {
        val lastWeek = WeekDateUtil.lastWeekStartDay(weekStartDay)
        loadedLastDay = WeekDateUtil.lastWeekStartDay(lastWeek)
        return intArrayOf(weekStartDay, lastWeek)
    }

    private fun getNewDays(day:Int) : IntArray {
        val yesterday = WeekDateUtil.getYesterday(day)
        val yesterday2 = WeekDateUtil.getYesterday(yesterday)
        val yesterday3 = WeekDateUtil.getYesterday(yesterday2)
        loadedLastDay = WeekDateUtil.getYesterday(yesterday3)
        return intArrayOf(day, yesterday, yesterday2, yesterday3)
    }
}