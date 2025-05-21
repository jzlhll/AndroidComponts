package com.allan.androidlearning.transfer.views

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.net.Uri
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.allan.androidlearning.R
import com.allan.androidlearning.transfer.MyDroidConst
import com.allan.androidlearning.transfer.benas.UriRealInfoEx
import com.au.module_android.Globals
import com.au.module_android.simplelivedata.asNoStickLiveData
import com.au.module_android.utils.ignoreError
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logd
import com.au.module_android.utils.serializableCompat
import com.au.module_android.utilsmedia.ContentUriRealPathType
import com.au.module_android.utilsmedia.getRealInfo
import com.au.module_android.utilsmedia.getRealPath
import com.au.module_android.utilsmedia.saveFileToPublicDirectory
import com.au.module_android.utilsmedia.shareFile
import com.au.module_androidui.dialogs.AbsActionDialogFragment
import com.au.module_androidui.dialogs.FragmentBottomSheetDialog
import com.au.module_androidui.toast.ToastBuilder
import kotlinx.coroutines.delay
import java.io.File
import java.lang.ref.WeakReference

class ExportSelectActionDialog(private var file: File? = null) : AbsActionDialogFragment() {
    companion object {
        /////////////////////////////////////////////
        var fileExportSuccessCallback: WeakReference<((info:String, exportFileStr:String)->Unit)>? = null
        var fileExportFailCallback:WeakReference<((String)->Unit)>? = null
        /**
         * 删除后的回调
         */
        var refreshFileListCallback:WeakReference<(()->Unit)>? = null

        var importSendCallback: WeakReference<()->Unit> ?= null

        ///////////////////////////////////////////// end

        fun pop(manager: FragmentManager,
                file:File,
                fileExportSuccessCallback:(info:String, exportFileStr:String)->Unit = {_,_->},
                fileExportFailCallback:(String)->Unit = {},
                refreshFileListCallback:()->Unit = {},
                importSendCallback:()->Unit = {}) {
            FragmentBottomSheetDialog.show<ExportSelectActionDialog>(manager, bundleOf("file" to file))
            this.fileExportSuccessCallback = WeakReference(fileExportSuccessCallback)
            this.fileExportFailCallback = WeakReference(fileExportFailCallback)
            this.refreshFileListCallback = WeakReference(refreshFileListCallback)
            this.importSendCallback = WeakReference(importSendCallback)
        }
    }

    val normalColor = ColorStateList.valueOf(Globals.getColor(com.au.module_androidcolor.R.color.color_text_normal))

    val mItems = listOf(
        ItemBean("share", "分享", R.drawable.ic_share, normalColor),
        ItemBean("sendImport", "导入到发送列表", R.drawable.ic_send, normalColor),
        ItemBean("delete", "删除", R.drawable.ic_delete, normalColor),
        ItemBean("exportOnly", "导出", R.drawable.ic_download, normalColor),
        ItemBean("exportAndDownload", "导出 & 删除", R.drawable.ic_download, normalColor))
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

    @SuppressLint("SdCardPath")
    private suspend fun exportInThread(file: File, delete: Boolean) {
        delay(0)

        //val info = exportFileToDownload(file.name , file)
        val uri = ignoreError {
            saveFileToPublicDirectory(
                Globals.app, file, delete,
                "MyDroidTransfer"
            )
        }

        if (delete) {
            refreshFileListCallback?.get()?.invoke()
        }

        val pair = uri?.getRealPath(Globals.app)
        if (pair != null) {
            val p = pair.first.replace("/storage/emulated/0/", "/sdcard/")
            when (pair.second) {
                ContentUriRealPathType.RelativePath -> {
                    fileExportSuccessCallback?.get()?.invoke("转存到${pair.first}成功！", "/sdcard/$p")
                }
                ContentUriRealPathType.FullPath -> {
                    fileExportSuccessCallback?.get()?.invoke("转存到${pair.first}成功！", p)
                }
            }
        } else {
            fileExportFailCallback?.get()?.invoke("转存失败！")
        }
    }

    override fun notify(tag: Any) {
        when (tag.toString()) {
            "share" -> {
                shareFile(Globals.app, file)
            }
            "exportOnly" -> {
                export(false)
            }
            "exportAndDownload" -> {
                export(true)
            }
            "sendImport" -> {
                val map = MyDroidConst.sendUriMap.realValue ?: hashMapOf()
                val oldValues = map.values

                val fileUri = Uri.fromFile(file!!)
                if (oldValues.find { it.uri == fileUri } != null) {
                    ToastBuilder().setMessage("已经存在发送列表中。").setIcon("info").setOnTopLater(200).toast()
                    return
                }
                val info = fileUri.getRealInfo(Globals.app)
                val infoEx = UriRealInfoEx.copyFrom(info)
                map.put(infoEx.uriUuid, infoEx)
                MyDroidConst.sendUriMap.asNoStickLiveData().setValueSafe(map)

                importSendCallback?.get()?.invoke()
            }
            "delete" -> {
                Globals.mainScope.launchOnThread {
                    ignoreError { file?.delete() }
                    delay(100)
                    refreshFileListCallback?.get()?.invoke()
                }
            }
        }
    }
}