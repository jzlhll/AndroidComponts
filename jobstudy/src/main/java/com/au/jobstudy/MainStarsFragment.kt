package com.au.jobstudy

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.au.jobstudy.check.CheckConsts
import com.au.jobstudy.databinding.FragmentMainFriendsBinding
import com.au.jobstudy.star.IStarBean
import com.au.jobstudy.star.StarAdapter
import com.au.jobstudy.star.StarHeadBean
import com.au.jobstudy.star.StarList
import com.au.jobstudy.star.StarMarkupBean
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.launchOnUi
import kotlinx.coroutines.launch

class MainStarsFragment : BindingFragment<FragmentMainFriendsBinding>() {
    private lateinit var adapter : StarAdapter

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.rcv.layoutManager = LinearLayoutManager(binding.rcv.context)
        binding.rcv.adapter = StarAdapter().also { adapter = it }

        StarList.allStarsLiveData.observe(viewLifecycleOwner) { allStars->
            lifecycleScope.launchOnThread {
                val names = CheckConsts.queryAllNamesDingToday(CheckConsts.currentDay())

                allStars.forEach { star->
                    star.isDing = names.contains(star.name)
                }

                val ret = mutableListOf<IStarBean>()
                ret.add(StarHeadBean("棒棒的，现在排名为第" + "<font color='red'>" + StarList.myRank + "</font>" + " !"))
                ret.addAll(allStars)
                ret.add(StarMarkupBean())

                lifecycleScope.launchOnUi {
                    adapter.submitList(ret, false)
                }
            }
        }
    }
}