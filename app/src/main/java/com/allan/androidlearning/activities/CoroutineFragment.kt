package com.allan.androidlearning.activities

import android.content.Context
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.allan.classnameanno.EntroFrgName
import com.au.module_android.click.onClick
import com.au.module_android.selectlist.SelectListFragment
import com.au.module_android.selectlist.SelectListItem
import com.au.module_android.utils.dp
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logt
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * @author allan
 * @date :2024/7/29 10:50
 * @description:
 */
@EntroFrgName
class CoroutineFragment(override val title: String = "Coroutine",
                              override val items: List<KotlinCoroutineSelectListItem> =
                                  listOf(KotlinCoroutineSelectListItem("Dispatchers"),
                                         KotlinCoroutineSelectListItem("Scope"),
                                         KotlinCoroutineSelectListItem("Test"),
                                  ),
                              override val initCur: KotlinCoroutineSelectListItem = KotlinCoroutineSelectListItem("Dispatchers"))
        : SelectListFragment<KotlinCoroutineSelectListItem>() {

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
            "Dispatchers" -> {
                logt { "Run0......" }

                lifecycleScope.launch {
                    try {
                        supervisorScope {
                            launchOnThread {
                                logt { "Run1......" }
                                Thread.sleep(2000)
                                logt { "Run1......end" }
                            }
                        }
                        supervisorScope {
                            launchOnThread {
                                logt { "Run2......" }
                                Thread.sleep(1000)
                                throw IllegalStateException()
                            }
                        }
                        logt { "Run112233" }
                    } catch (e:Exception) {
                        e.printStackTrace()
                    }
                }
            }

            "Test"-> {
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