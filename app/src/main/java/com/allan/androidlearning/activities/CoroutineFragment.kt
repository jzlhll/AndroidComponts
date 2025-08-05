package com.allan.androidlearning.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.click.onClick
import com.au.module_android.selectlist.SelectListFragment
import com.au.module_android.selectlist.SelectListItem
import com.au.module_android.utils.ALogJ
import com.au.module_android.utils.dp
import com.au.module_android.utils.logd
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.logt
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

/**
 * @author allan
 * @date :2024/7/29 10:50
 * @description:
 */
@EntryFrgName(priority = 100)
class CoroutineFragment(override val title: String = "Coroutine")
        : SelectListFragment<KotlinCoroutineSelectListItem>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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

    val initItem = KotlinCoroutineSelectListItem("子线程") {

    }

    val testItem = KotlinCoroutineSelectListItem("测试") {

        lifecycleScope.launch {
            ALogJ.t("start...")
            val deferred1 = async {
                delay(1000)
                "1111"
            }
            val deferred2 = async {
                delay(800)
                "2222"
            }
            val data1 = deferred1.await()
            val data2 = deferred2.await()
            //等待他们完成
            ALogJ.t("data1 $data1, data2 $data2")
        }
    }

    val testItem2 = KotlinCoroutineSelectListItem("测试2") {
        lifecycleScope.launch(CoroutineExceptionHandler {
            e, t->
            t.printStackTrace()
        }) {
            val deferred = async(Dispatchers.IO) {
                //指定运行到子线程
                Thread.sleep(3000)
                """ {"data":"request successfully."} """
            }
            ALogJ.t("运行在主线程")
            val data = deferred.await()
            ALogJ.t("运行在主线程得到结果 $data")
        }
    }

    private val _items = listOf(
        initItem,
        KotlinCoroutineSelectListItem("主线程") {

        },
        KotlinCoroutineSelectListItem("主线程2") {

        },
        KotlinCoroutineSelectListItem("主线程3") {

        },
        testItem,
        testItem2
    )

    override val initCur: KotlinCoroutineSelectListItem
        get() = initItem

    override val items: List<KotlinCoroutineSelectListItem>
        get() = _items

    override fun bindItemView(v: View, item:KotlinCoroutineSelectListItem, isSelect: Boolean) {
        v as MaterialButton
        v.text = item.itemName
        v.onClick(item.onClick)
    }
}

class KotlinCoroutineSelectListItem(override val itemName: String, val onClick: (View)->Unit) : SelectListItem()