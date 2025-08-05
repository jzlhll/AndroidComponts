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
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
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

    val initItem = KotlinCoroutineSelectListItem("子线程") {

    }


    private val testScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val testItem = KotlinCoroutineSelectListItem("测试") {
        lifecycleScope.launch {
            val deferred = async(Dispatchers.IO) {
                //指定运行到子线程
                Thread.sleep(3000)
                throw RuntimeException("error crash")
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