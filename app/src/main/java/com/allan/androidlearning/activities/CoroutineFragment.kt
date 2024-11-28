package com.allan.androidlearning.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.allan.androidlearning.crashtest.debugOtherActivityCreateCrash
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.selectlist.SelectListFragment
import com.au.module_android.selectlist.SelectListItem
import com.au.module_android.utils.dp
import com.au.module_android.utils.logd
import com.au.module_android.utils.logt
import com.au.module_android.utils.unsafeLazy
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CoroutineViewModel : ViewModel() {
    fun run() {
        logt { "viewModel: run1" }
        viewModelScope.launch {
            logt { "viewModel: run in...." }
        }
        logt { "viewModel: run2" }
    }
}

/**
 * @author allan
 * @date :2024/7/29 10:50
 * @description:
 */
@EntryFrgName
class CoroutineFragment(override val title: String = "Coroutine",
                              override val items: List<KotlinCoroutineSelectListItem> =
                                  listOf(KotlinCoroutineSelectListItem("子线程"),
                                         KotlinCoroutineSelectListItem("主线程"),
                                        KotlinCoroutineSelectListItem("主线程2"),
                                        KotlinCoroutineSelectListItem("主线程3"),
                                        KotlinCoroutineSelectListItem("主线程4"),
                                        KotlinCoroutineSelectListItem("Test"),
                                  ),
                              override val initCur: KotlinCoroutineSelectListItem = KotlinCoroutineSelectListItem("子线程"))
        : SelectListFragment<KotlinCoroutineSelectListItem>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        debugOtherActivityCreateCrash()
    }

    private val vm by unsafeLazy { ViewModelProvider(this)[CoroutineViewModel::class.java] }

    private val subScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun itemHeight(): Int {
        return 48.dp
    }

    override fun itemTopMargin(): Int {
        return 8.dp
    }

    override fun itemPaddingHorz(): Int {
        return 20.dp
    }

    override fun createItemView(context: Context): View {
        return MaterialButton(context)
    }

    private val onClick:(View)->Unit = {
        val item = it.tag as KotlinCoroutineSelectListItem
        when (item.itemName) {
            "子线程" -> {
                subScope.launch {
                    logt { "Run1......" }
                    delay(100)
                    throw RuntimeException("Error Exception") //不论是这种
//                    launch { ////或者
//                        throw RuntimeException("Error Exception")
//                    }
//                    launch(Dispatchers.Main) { //还是这种
//                        throw RuntimeException("Error Exception")
//                    }
                }
                subScope.launch {
                    logt { "Run2......" }
                    delay(200)
                    logt { "Run2......over" }
                }
                subScope.launch {
                    logt { "Run3......" }
                    delay(300)
                    logt { "Run3......over" }
                }

            }

            "主线程" -> {
                val a =  10 / 0
                logt { "a = $a" }
            }
            "主线程2" -> {
                lifecycleScope.launch {
                    val a =  10 / 0
                    logt { "a = $a" }
                }
            }
            "主线程3" -> {
                Globals.mainScope.launch {
                    delay(100)
                    val a =  10 / 0
                    logt { "a = $a" }
                }
            }
            "主线程4" -> {
                Globals.mainHandler.postDelayed({
                    val a =  10 / 0
                    logt { "a = $a" }
                }, 100)
            }

            "Test"-> {
                subScope.launch(CoroutineExceptionHandler { coroutineContext, throwable ->
                    logt { "parent handler: ${throwable.message}" }
                }) {
                    launch(CoroutineExceptionHandler { coroutineContext, throwable ->
                        logt { "child handler: ${throwable.message}" }
                    }) {
                        delay(100)
                        logt { "run 111" }
                        throw RuntimeException("Run Exception")
                    }
                    launch {
                        delay(200)
                        logd { "run 2222" }
                    }
                }
            }
        }
    }

    override fun bindItemView(v: View, item:KotlinCoroutineSelectListItem, isSelect: Boolean) {
        v as MaterialButton
        v.text = item.itemName
        v.onClick(onClick)
    }
}

class KotlinCoroutineSelectListItem(override val itemName: String) : SelectListItem()