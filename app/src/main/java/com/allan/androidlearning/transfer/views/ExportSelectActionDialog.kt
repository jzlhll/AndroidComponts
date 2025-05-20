package com.allan.androidlearning.transfer.views

import com.allan.androidlearning.R
import com.allan.androidlearning.transfer.MyDroidConst
import com.au.module_android.Globals
import com.au.module_android.utils.ignoreError
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logd
import com.au.module_android.utils.serializableCompat
import com.au.module_android.utilsmedia.ContentUriRealPathType
import com.au.module_android.utilsmedia.exportFileToDownload
import com.au.module_android.utilsmedia.getRealPath
import com.au.module_android.utilsmedia.saveFileToPublicDirectory
import com.au.module_androidui.dialogs.AbsActionDialogFragment
import kotlinx.coroutines.delay
import java.io.File

class ExportSelectActionDialog(private var file: File? = null) : AbsActionDialogFragment() {
    val mItems = listOf<ItemBean>(
        ItemBean("exportOnly", "导出", R.drawable.ic_download),
        ItemBean("exportAndDownload", "导出&删除", R.drawable.ic_download))
    override val items: List<ItemBean>
        get() = mItems

    override fun onStart() {
        super.onStart()
        file = arguments?.serializableCompat<File>("file")
    }

    private fun export(delete: Boolean) {
        //todo new function compact
        Globals.mainScope.launchOnThread {
            logd { "export file: $file" }
            exportInThread(file!!, delete)
        }
    }

    private suspend fun exportInThread(file: File, delete: Boolean) {
        delay(0)

        //val info = exportFileToDownload(file.name , file)
        val uri = ignoreError {
            saveFileToPublicDirectory(
                Globals.app, file, delete,
                "MyDroidTransfer"
            )
        }

        val pair = uri?.getRealPath(Globals.app)
        if (pair != null) {
            val p = pair.first.replace("/storage/emulated/0/", "/sdcard/")
            when (pair.second) {
                ContentUriRealPathType.RelativePath -> {
                    MyDroidConst.fileExportSuccessCallbacks.forEach {
                        it("转存到${pair.first}成功！", "/sdcard/$p")
                    }
                }
                ContentUriRealPathType.FullPath -> {
                    MyDroidConst.fileExportSuccessCallbacks.forEach {
                        it("转存到${pair.first}成功！", p)
                    }
                }
            }
        } else {
            MyDroidConst.fileExportFailCallbacks.forEach {
                it("转存失败！")
            }
        }
    }

    private suspend fun exportInThread2(file: File) {
        val info = exportFileToDownload(file.name , file)
        if (!info.contains("error")) {
            ignoreError { file.delete() }
            logd { "export file info: $info" }
            val splits = info.split("\n")
            MyDroidConst.fileExportSuccessCallbacks.forEach {
                it(splits[0], splits[1])
            }
        } else {
            MyDroidConst.fileExportFailCallbacks.forEach {
                it(info)
            }
        }
    }

    override fun notify(tag: Any) {
        when (tag.toString()) {
            "exportOnly" -> {
                export(false)
            }
            "exportAndDownload" -> {
                export(true)
            }
        }
    }
}