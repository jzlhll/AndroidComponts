package com.allan.mydroid.views.send

import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.allan.mydroid.beansinner.UriRealInfoEx
import com.allan.mydroid.globals.MyDroidConst
import com.au.module_android.utils.gone
import com.au.module_android.utils.visible

abstract class SendListSelectorCommon(val f : Fragment, noSelectBtns:Boolean) {
    private val adapter = SendListAdapter(noSelectBtns) {it, mode->
        itemClick(it, mode)
    }

    abstract fun rcv(): RecyclerView
    abstract fun empty(): TextView

    abstract fun itemClick(bean: UriRealInfoEx?, mode:String)
    
    fun onBindingCreated() {
        initRcv()
    }

    fun isEmptyList() = adapter.datas.isEmpty()

    private fun initRcv() {
        val rcv = rcv()
        rcv.adapter = adapter
        rcv.layoutManager = LinearLayoutManager(rcv.context)
        rcv.setHasFixedSize(true)

        MyDroidConst.sendUriMap.observe(f) { map-> //监听没问题
            val list = ArrayList<UriRealInfoEx>()
            list.addAll(map.values)
            adapter.submitList(list, false)
            if (list.isEmpty()) {
                empty().visible()
            } else {
                empty().gone()
            }
        }
    }
}