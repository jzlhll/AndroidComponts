package com.au.jobstudy

import android.content.Context
import android.os.Bundle
import android.view.View
import com.au.jobstudy.bean.DataItem
import com.au.jobstudy.databinding.FragmentCheckInBinding
import com.au.module_android.ui.bindings.BindingParamsFragment

/**
 * @author au
 * @date :2023/12/29 17:04
 * @description:
 */
class CheckInFragment : BindingParamsFragment<FragmentCheckInBinding>() {
    companion object {
        fun start(context:Context, dataItem:DataItem) {
            putTempParams(CheckInFragment::class.java, "dataItem" to dataItem)
        }
    }

    private val dataItem = getTempParams<DataItem>("dataItem")!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.title.text = dataItem.subject
        binding.descText.text = dataItem.desc
    }
}