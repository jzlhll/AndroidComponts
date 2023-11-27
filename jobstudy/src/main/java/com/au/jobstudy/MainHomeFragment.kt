package com.au.jobstudy

import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.allan.nested.layout.SimpleItemsLayout
import com.au.jobstudy.bean.Subject
import com.au.jobstudy.bean.nameToSubject
import com.au.jobstudy.databinding.FragmentMainHomeBinding
import com.au.jobstudy.databinding.HomeStarOnlyOneBigBinding
import com.au.jobstudy.databinding.HomeStarThreeStarsBinding
import com.au.jobstudy.home.ThisWeekEachLayoutData
import com.au.jobstudy.home.ThisWeekLayoutData
import com.au.jobstudy.util.currentDay
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.visible
import kotlinx.coroutines.launch

class MainHomeFragment : BindingFragment<FragmentMainHomeBinding>() {
    private lateinit var viewModel:GlobalDataViewModel

    override fun afterViewCreated(savedInstanceState: Bundle?, viewBinding: FragmentMainHomeBinding) {
        viewModel = ViewModelProvider(requireActivity())[GlobalDataViewModel::class.java]

        binding.thisWeekList.itemInflateCreator = object : ((LayoutInflater, SimpleItemsLayout, Boolean, Any)->ViewBinding) {
            override fun invoke(layoutInflate: LayoutInflater, me: SimpleItemsLayout, attachedToParent: Boolean, data: Any): ViewBinding {
                return when (data) {
                    is ThisWeekLayoutData-> {
                        HomeStarOnlyOneBigBinding.inflate(layoutInflate, me, attachedToParent).also {
                            it.numbersTv.text = "${data.num}"
                        }
                    }

                    is ThisWeekEachLayoutData-> {
                        HomeStarThreeStarsBinding.inflate(layoutInflate, me, attachedToParent).also { vb->
                            var count = 0
                            data.eachStars.forEach {
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

                    else -> {
                        throw RuntimeException("No way.")
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            val list = viewModel.getWeekData(currentDay)
            var totalCount = 0
            val allSubjectCount = HashMap<Subject, Int>()
            list.forEach { dataItem->
                totalCount++
                val subj = nameToSubject(dataItem.subject)
                if (allSubjectCount.containsKey(subj)) {
                    allSubjectCount[subj] = allSubjectCount[subj]!! + 1
                } else {
                    allSubjectCount[subj] = 1
                }
            }
            //总计的星星个数
            binding.thisWeekList.addItem(ThisWeekLayoutData(totalCount))
            val keySet = allSubjectCount.keys.toList()
            val first3Set = HashSet<Subject>()
            val second3Set = HashSet<Subject>()

            val count = keySet.size
            var startCount = 0
            while (startCount < 3 && startCount < count) {
                first3Set.add(keySet[startCount])
                startCount++
            }
            while (startCount < count) {
                second3Set.add(keySet[startCount])
                startCount++
            }

            //两行3颗星星
            if (first3Set.size > 0) {
                val map = first3Set.map {
                    Pair(it, allSubjectCount[it]!!)
                }
                binding.thisWeekList.addItem(ThisWeekEachLayoutData(map.toTypedArray()))
            }
            if (second3Set.size > 0) {
                val map = second3Set.map {
                    Pair(it, allSubjectCount[it]!!)
                }
                binding.thisWeekList.addItem(ThisWeekEachLayoutData(map.toTypedArray()))
            }
        }
    }
}