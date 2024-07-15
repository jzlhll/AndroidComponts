package com.au.jobstudy

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.allan.nested.decoration.PaddingItemDecoration
import com.au.jobstudy.check.NameList
import com.au.jobstudy.utils.Dayer
import com.au.jobstudy.utils.WeekDateUtil
import com.au.jobstudy.utils.WeekDateUtil.currentTimeToHelloGood
import com.au.jobstudy.databinding.FragmentMainHomeBinding
import com.au.jobstudy.home.HomeRcvAdapter
import com.au.jobstudy.home.HomeRcvItemBean
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.dp

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
//
//        binding.passFab.onClick {_->
//            Pass().useOnePassCount {
//                if (it > 0) {
//                    toastOnTop("今天已经免做啦！还有${it}次机会!")
//                } else {
//                    toastOnTop("今天免做啦！本周免做机会已用完。")
//                }
//            }
//        }
    }

    override fun onResume() {
        super.onResume()

        binding.weather.text = WeekDateUtil.getTodayWeekN()

        val time = String.format(getString(R.string.name_hello_format), userName, currentTimeToHelloGood())
        if (binding.title.text != time) {
            binding.title.text = time
            val curDay = Dayer()
            //todo
        }
    }

    override fun onPause() {
        super.onPause()
    }
}