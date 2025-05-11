package com.allan.androidlearning.transfer

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.allan.androidlearning.databinding.HolderMydroidFileitemBinding
import com.allan.androidlearning.transfer.benas.MergedFileInfo
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.ignoreError
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logd
import com.au.module_android.utils.serializableCompat
import com.au.module_android.utilsmedia.ContentUriRealPathType
import com.au.module_android.utilsmedia.MediaHelper
import com.au.module_android.utilsmedia.exportFileToDownload
import com.au.module_android.utilsmedia.getRealPath
import com.au.module_android.utilsmedia.saveFileToPublicDirectory
import com.au.module_android.utilsmedia.shareFile
import com.au.module_androidui.dialogs.AbsActionDialogFragment
import com.au.module_androidui.dialogs.FragmentBottomSheetDialog
import com.au.module_nested.recyclerview.BindRcvAdapter
import com.au.module_nested.recyclerview.viewholder.BindViewHolder
import kotlinx.coroutines.delay
import java.io.File

class MyDroidTransferFileListAdapter(val f:Fragment) : BindRcvAdapter<MergedFileInfo, MyDroidTransferViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyDroidTransferViewHolder {
        return MyDroidTransferViewHolder(create(parent))
    }
}

class MyDroidTransferViewHolder(binding: HolderMydroidFileitemBinding) : BindViewHolder<MergedFileInfo, HolderMydroidFileitemBinding>(binding) {
    init {
        binding.downloadBtn.onClick {
            val d = currentData ?: return@onClick
            val file = d.file
            val fragmentMgr = (bindingAdapter as MyDroidTransferFileListAdapter).f.childFragmentManager

            FragmentBottomSheetDialog.show<ExportSelectActionDialog>(fragmentMgr, bundleOf("file" to file))
        }

        binding.sharesBtn.onClick {
            val d = currentData ?: return@onClick
            shareFile(Globals.app, d.file)
        }
    }

    @DrawableRes
    fun getIcon(fileName: String): Int {
        // 提取文件后缀并转为小写（处理无后缀的情况）
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return when (extension) {
            // 文本/文档类型
            "doc", "docx" -> com.allan.androidlearning.R.drawable.ic_filetype_doc
            "xls", "xlsx" -> com.allan.androidlearning.R.drawable.ic_filetype_xls
            "pdf" -> com.allan.androidlearning.R.drawable.ic_filetype_pdf
            "txt", "log", "md" -> com.allan.androidlearning.R.drawable.ic_filetype_text
            // 压缩包类型
            "zip", "rar", "tar", "gz", "7z" -> com.allan.androidlearning.R.drawable.ic_filetype_archive
            // 代码文件类型（可选扩展）
            "java", "kt", "py", "js", "html", "css" -> com.allan.androidlearning.R.drawable.ic_filetype_code
            // 其他类型
            else ->
                if (MediaHelper.isImageFileSimple(extension)) {
                    com.allan.androidlearning.R.drawable.ic_filetype_image
                } else if (MediaHelper.isAudioFileSimple(extension)) {
                    com.allan.androidlearning.R.drawable.ic_filetype_audio
                } else if (MediaHelper.isVideoFileSimple(extension)) {
                    com.allan.androidlearning.R.drawable.ic_filetype_video
                } else {
                    com.allan.androidlearning.R.drawable.ic_filetype_other
                }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun bindData(bean: MergedFileInfo) {
        super.bindData(bean)
        binding.fileNameTv.text = bean.file.name
        binding.fileSizeAndMD5Tv.text = bean.md5 + "   " + bean.fileSizeInfo
        binding.icon.setImageResource(getIcon(bean.file.name))
    }
}

class ExportSelectActionDialog(private var file: File? = null) : AbsActionDialogFragment() {
    val mItems = listOf<ItemBean>(
        ItemBean("exportOnly", "导出", com.allan.androidlearning.R.drawable.ic_download),
        ItemBean("exportAndDownload", "导出&删除", com.allan.androidlearning.R.drawable.ic_download))
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
                    MyDroidGlobalService.fileExportSuccessCallbacks.forEach {
                        it("转存到${pair.first}成功！", "/sdcard/$p")
                    }
                }
                ContentUriRealPathType.FullPath -> {
                    MyDroidGlobalService.fileExportSuccessCallbacks.forEach {
                        it("转存到${pair.first}成功！", p)
                    }
                }
            }
        } else {
            MyDroidGlobalService.fileExportFailCallbacks.forEach {
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
            MyDroidGlobalService.fileExportSuccessCallbacks.forEach {
                it(splits[0], splits[1])
            }
        } else {
            MyDroidGlobalService.fileExportFailCallbacks.forEach {
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

