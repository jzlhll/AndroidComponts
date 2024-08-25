package com.allan.autoclickfloat.activities.recordprojects

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import com.allan.autoclickfloat.databinding.AllAppListFragmentBinding
import com.au.module_android.ui.bindings.BindingFragment

/**
 * @author allan
 * @date :2024/6/5 15:05
 * @description:
 */
class AllAppListFragment : BindingFragment<AllAppListFragmentBinding>() {
    val adapter = AllAppListAdapter()
    var appList:List<ApplicationInfo>? = null
    private var index = 0
    private var page = 28

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        adapter.bind(binding.rcv)
        adapter.loadMoreAction = {
            adapter.appendDatas(nextPage())
        }
        val pm = requireActivity().packageManager
        appList = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        adapter.initDatas(nextPage())
    }

    fun nextPage() : List<AllAppListItemBean> {
        var i = 0
        val appListSize = appList?.size ?: 0
        val pm = requireActivity().packageManager

        val list = ArrayList<AllAppListItemBean>()

        while (i < page && index < appListSize) {
            val info = appList!![index]
            val bean = AllAppListItemBean(getAppLabelByPackageName(pm, info) ?: info.packageName,
                info.packageName,
                getAppIconByPackageName(pm, info.packageName))
            list.add(bean)
            i++
            index++
        }
        return list
    }

    fun getAppIconByPackageName(m: PackageManager, packageName: String): Drawable? {
        return try {
            m.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null // 应用未安装或者获取失败
        }
    }

    fun getAppLabelByPackageName(m: PackageManager, info: ApplicationInfo): CharSequence? {
        return try {
            m.getApplicationLabel(info)
        } catch (e: PackageManager.NameNotFoundException) {
            null // 应用未安装或者获取失败
        }
    }
}