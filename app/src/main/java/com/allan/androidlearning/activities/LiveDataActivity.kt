package com.allan.androidlearning.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.*
import com.allan.androidlearning.R
import com.allan.androidlearning.databinding.ActivityLiveDataBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class LiveDataActivity : AppCompatActivity() {
    private val numbersVM by viewModels<NumbersViewModel>()
    private val TAG = LiveDataActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLiveDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //liveData 是粘性的。只有有值，一个直接observe进来的监听者立刻得到回调
        numbersVM.progressLiveData.postValue(100)

        numbersVM.viewModelScope.launch {
            withContext(Dispatchers.Default) {
                delay(3000)

            }
            numbersVM.progressLiveData.observe(this@LiveDataActivity) {
                Log.d(TAG, "liveData changed $it")
            }
        }

        binding.changeBtn.setOnClickListener {
            numbersVM.progressLiveData.postValue((Math.random()*100).toInt())
        }

    }
}

public class NumbersViewModel : ViewModel() {
    val progressLiveData:MutableLiveData<Int> = MutableLiveData<Int>()
}
