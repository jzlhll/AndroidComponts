package com.allan.androidlearning.activities

import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allan.androidlearning.databinding.ActivityLiveDataBinding
import com.allan.androidlearning.utils.logm
import com.allan.androidlearning.utils.testGsonData
import com.au.module_android.arct.BaseBindingFragment
import com.au.module_android.simplelivedata.StatusLiveData
import com.au.module_android.simplelivedata.StatusNoStickLiveData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LiveDataFragment : BaseBindingFragment<ActivityLiveDataBinding>() {
    private val viewModel by viewModels<MyViewModel>()

    override fun onCommonAfterCreateView(
        owner: LifecycleOwner,
        savedInstanceState: Bundle?,
        resources: Resources,
    ) {
        //liveData 是粘性的。只有有值，一个直接observe进来的监听者立刻得到回调
        viewModel.noStickLiveData.observe(this) {
            logm("noStickLiveData receiver: ${it.data} ${it.code} ${it.message} ${it.status}")
        }
        viewModel.noStickLiveDefData.observe(this) {
            logm("noStickLiveDefData receiver: ${it.data} ${it.code} ${it.message} ${it.status}")
        }
        viewModel.normalData.observe(this) {
            logm("normalData receiver: ${it.data} ${it.code} ${it.message} ${it.status}")
        }
        viewModel.normalDefData.observe(this) {
            logm("normalDefData receiver: ${it.data} ${it.code} ${it.message} ${it.status}")
        }

        binding.changeBtn.setOnClickListener {
            viewModel.changeData()
        }

        testGsonData()
    }

}

class MyViewModel : ViewModel() {
    val normalData = StatusLiveData<String>()
    val normalDefData = StatusLiveData("hello")

    val noStickLiveData = StatusNoStickLiveData<String>()
    val noStickLiveDefData = StatusNoStickLiveData("no stick init data")

    fun changeData() {
        viewModelScope.launch {
            delay(1000)
            normalData.success("new normal", 10, "normal new one!")
            normalDefData.error(null, -1, "error normal def!")

            noStickLiveData.success("new stick", 10, "a")
            noStickLiveDefData.success("new stick def", -2, "bb")
        }
    }
}
