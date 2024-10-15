package com.allan.androidlearning.activities

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allan.androidlearning.databinding.FragmentLiveDataBinding
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.ui.bindings.BindingFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@EntryFrgName
class LiveDataFragment : BindingFragment<FragmentLiveDataBinding>() {
    private val viewModel by viewModels<MyViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //liveData 是粘性的。只有有值，一个直接observe进来的监听者立刻得到回调
        binding.changeBtn.setOnClickListener {
            viewModel.changeData()
        }
    }

}

class MyViewModel : ViewModel() {

    val noStickData = NoStickLiveData<String>()
    val noStickDataDef = NoStickLiveData("StickDef")

    fun changeData() {
        viewModelScope.launch {
            delay(1000)

            noStickData.setValueSafe("new stick 11")
            noStickDataDef.setValueSafe(null)
        }
    }
}
