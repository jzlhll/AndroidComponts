package com.au.jobstudy

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.au.jobstudy.databinding.FragmentMainFriendsBinding
import com.au.jobstudy.star.IStarBean
import com.au.jobstudy.star.StarAdapter
import com.au.jobstudy.star.StarHeadBean
import com.au.jobstudy.star.StarConsts
import com.au.jobstudy.star.StarItemBean
import com.au.jobstudy.star.StarMarkupBean
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.launchOnUi

class MainStarsFragment : BindingFragment<FragmentMainFriendsBinding>() {
    private lateinit var adapter : StarAdapter

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.rcv.layoutManager = LinearLayoutManager(binding.rcv.context)
        binding.rcv.adapter = StarAdapter(this).also { adapter = it }

        observerAllStarLiveData()

        StarConsts.mineStarData.observe(viewLifecycleOwner) {
            adapter.datas.forEachIndexed { index, iStarBean ->
                //later：这里只管star变化更新我的item。dingCount暂时item自行处理
                if (iStarBean is StarItemBean && iStarBean.name == it.name) {
                    iStarBean.starNum = it.starCount
                    adapter.notifyItemChanged(index)
                    return@observe
                }
            }
        }
    }

    private fun observerAllStarLiveData() {
        StarConsts.allStarsLiveData.observe(viewLifecycleOwner) { allStars ->
            lifecycleScope.launchOnThread {
                val ret = mutableListOf<IStarBean>()
                ret.add(StarHeadBean("棒棒的，现在排名为第" + "<font color='red'>" + StarConsts.myRank + "</font>" + " !"))
                ret.addAll(allStars)
                ret.add(StarMarkupBean())

                lifecycleScope.launchOnUi {
                    adapter.submitList(ret, false)
                }
            }
        }
    }
}