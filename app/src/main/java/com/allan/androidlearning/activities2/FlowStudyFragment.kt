package com.allan.androidlearning.activities2

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.click.onClick
import com.au.module_android.simpleflow.StatusState
import com.au.module_android.ui.views.ViewFragment
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logd
import com.au.module_android.utils.logt
import com.au.module_android.widget.CustomFontText
import com.au.module_androidui.toast.ToastBuilder
import com.au.module_androidui.toast.ToastUtil
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMap
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import kotlin.concurrent.thread
import kotlin.jvm.Throws

@EntryFrgName(priority = 200)
class FlowStudyFragment : ViewFragment() {
    val viewModel by lazy { FlowStudyViewModel() }
    private lateinit var showInfoTv : CustomFontText

    override fun onUiCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return LinearLayout(requireActivity()).also { ll->
            ll.orientation = LinearLayout.VERTICAL

            ll.addView(CustomFontText(requireActivity()).also {
                showInfoTv = it
            })
            ll.addView(MaterialButton(requireActivity()).also {
                it.text = "test1"
                it.onClick {

                }
            })

            lifecycleScope.launch {
                viewModel.dataState.collect {
                    it.parse(
                        loading = {
                        },
                        success = { data->
                            showInfoTv.text = data
                        },
                        error = { exMsg->
                            ToastBuilder().setMessage(exMsg).setOnTop().toast()
                        }
                    )
                }
            }

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
//                    listOf(1, 2, 3, 4, 5).asFlow()
//                        .onEach {
//                            delay(100)
//                        }.collect { data->
//                           logt { "flow $data" }
//                        }

//                    channelFlow {
//                        for (i in 1..5) {
//                            delay(100)
//                            send(i)
//                        }
//                    }.collect{ data->
//                        logt { "flow $data" }
//                    }

                    logt { "doing0..." }
                    launch {
                        viewModel.callbackFlow.collect {  d->
                            logt { "callback flow $d" }
                        }
                    }

                    logt { "doing1..." }
                    viewModel.channelStart()
                    launch {
                        viewModel.channel.consumeEach { data ->
                            logt { "channel getData : $data" }
                        }
                    }
                    logt { "doing2..." }
                    logt { "doing3..." }


                }
            }
        }
    }
}

class FlowStudyViewModel : ViewModel() {
    val channel = Channel<Int>(capacity = 10) // 缓冲大小为 10 的 Channel，传输 Int 类型数据
    fun channelStart() {
        viewModelScope.launch {
            for (i in 1..10) {
                channel.send(i) // 向 Channel 发送 1 到 10 的整数
            }
            channel.close() // 数据发送完毕，关闭 Channel
        }
    }

    private val _dataState = MutableStateFlow<StatusState<String>>(StatusState.Loading)
    val dataState: StateFlow<StatusState<String>> = _dataState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<MyEvent>()
    val eventFlow: SharedFlow<MyEvent> = _eventFlow.asSharedFlow()

    val callbackFlow = callbackFlow {
        //模拟网络请求回调
        try {
            requestCallbackFlowApi { result ->
                //发送数据
                trySend(result).isSuccess
                //close(null)
            }
        } catch (e: Exception) {
            close(e)
        }

        awaitClose { //必须有awaitClose的设定，否则程序报错。

        }
    }

    /**
     * 模拟网络请求
     */
    @Throws
    fun requestCallbackFlowApi(callback: (Int) -> Unit) {
        thread {
            Thread.sleep(3000)
            callback(3)
        }
    }

    fun sendEvent(event: MyEvent) {
        viewModelScope.launch {
            _eventFlow.emit(event)
        }
    }

    fun startLoad() {
        viewModelScope.launch {
//            _dataState.updateAndGet { //可以比较旧值一致就不通知出去
//                try {
//                    //val data = api.request()
//                    val data = "" + Math.random() * 10000
//                    DataState.Success(data)
//                } catch (e: Exception) {
//                    DataState.Error(e.message)
//                }
//            }
            _dataState.value = try {
                //val data = api.request()
                delay(200) //模拟耗时
                val r = Math.random()
                if (r < 0.3) {
                    StatusState.Error("Server tell me error!")
                } else if (r < 0.6) {
                    val data = "" + r * 10000
                    StatusState.Success(data)
                } else {
                    throw RuntimeException("parse error")
                }
            } catch (e: Exception) {
                StatusState.Error(e.message)
            }
        }
    }
}

sealed class MyEvent {
    object EventA : MyEvent()
    object EventB : MyEvent()
}