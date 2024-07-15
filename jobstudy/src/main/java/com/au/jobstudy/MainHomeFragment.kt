package com.au.jobstudy

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.allan.nested.decoration.PaddingItemDecoration
import com.au.jobstudy.check.CheckConsts
import com.au.jobstudy.check.NameList
import com.au.jobstudy.check.StarList
import com.au.jobstudy.utils.Dayer
import com.au.jobstudy.utils.WeekDateUtil
import com.au.jobstudy.utils.WeekDateUtil.currentTimeToHelloGood
import com.au.jobstudy.databinding.FragmentMainHomeBinding
import com.au.jobstudy.home.HomeRcvAdapter
import com.au.jobstudy.home.HomeRcvItemBean
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.dp
import kotlinx.coroutines.launch

class MainHomeFragment : BindingFragment<FragmentMainHomeBinding>() {
    private lateinit var adapter: HomeRcvAdapter
    private val userName = NameList.NAMES_JIANG_TJ

    private val itemClick : (HomeRcvItemBean)->Unit = { itemBean->
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

        CheckConsts.workChangedLiveData.observe(viewLifecycleOwner) {

        }

        CheckConsts.completedChangedLiveData.observe(viewLifecycleOwner) {

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

    override fun onPause() {
        super.onPause()
    }
}