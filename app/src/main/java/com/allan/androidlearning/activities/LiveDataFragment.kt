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
import com.au.module_android.ui.BaseBindingFragment
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.simplelivedata.StatusLiveData
import com.au.module_android.simplelivedata.StatusNoStickLiveData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LiveDataFragment : BaseBindingFragment<ActivityLiveDataBinding>() {
    private val viewModel by viewModels<MyViewModel>()

    override fun onAfterCreatedView(
        owner: LifecycleOwner,
        savedInstanceState: Bundle?,
        resources: Resources,
    ) {
        //liveData 是粘性的。只有有值，一个直接observe进来的监听者立刻得到回调
        viewModel.noStickData.observe(this) {
            logm("noStickLiveData receiver: $it")
        }
        viewModel.noStickDataDef.observe(this) {
            logm("noStickLiveDefData receiver: $it")
        }

        binding.changeBtn.setOnClickListener {
            viewModel.changeData()
        }

        testGsonData()
    }

}

class MyViewModel : ViewModel() {

    val noStickData = NoStickLiveData<String>()
    val noStickDataDef = NoStickLiveData<String>("StickDef")

    fun changeData() {
        viewModelScope.launch {
            delay(1000)

            noStickData.setValueSafe("new stick 11")
            noStickDataDef.setValueSafe("new stick 22")
        }
    }
}
