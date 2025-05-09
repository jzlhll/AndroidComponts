package com.allan.androidlearning.transfer

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import com.allan.androidlearning.activities2.MyDroidTransferFragment
import com.allan.androidlearning.databinding.HolderMydroidFileitemBinding
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.utils.ignoreError
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logd
import com.au.module_android.utilsmedia.ContentUriRealPathType
import com.au.module_android.utilsmedia.MediaHelper
import com.au.module_android.utilsmedia.exportFileToDownload
import com.au.module_android.utilsmedia.getRealPath
import com.au.module_android.utilsmedia.saveFileToPublicDirectory
import com.au.module_android.utilsmedia.shareFile
import com.au.module_nested.recyclerview.BindRcvAdapter
import com.au.module_nested.recyclerview.viewholder.BindViewHolder
import kotlinx.coroutines.delay
import java.io.File

class MyDroidTransferFileListAdapter : BindRcvAdapter<MergedFileInfo, MyDroidTransferViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyDroidTransferViewHolder {
        return MyDroidTransferViewHolder(create(parent))
    }
}

class MyDroidTransferViewHolder(binding: HolderMydroidFileitemBinding) : BindViewHolder<MergedFileInfo, HolderMydroidFileitemBinding>(binding) {
    init {
        binding.downloadBtn.onClick {
            val d = currentData ?: return@onClick
            val file = d.file
            //todo new function compact
            Globals.mainScope.launchOnThread {
                logd { "export file: $file" }
                exportInThread(file)
            }
        }

        binding.sharesBtn.onClick {
            val d = currentData ?: return@onClick
            shareFile(Globals.app, d.file)
        }
    }

    private suspend fun exportInThread(file: File) {
        delay(0)

        //val info = exportFileToDownload(file.name , file)
        val uri = ignoreError {
            saveFileToPublicDirectory(
                Globals.app, file, false,
                "MyDroidTransfer"
            )
        }

        val pair = uri?.getRealPath(Globals.app)
        if (pair != null) {
            val p = pair.first.replace("/storage/emulated/0/", "/sdcard/")
            when (pair.second) {
                ContentUriRealPathType.RelativePath -> {
                    MyDroidTransferFragment.fileExportSuccessCallback?.invoke("转存到${pair.first}成功！", "/sdcard/$p")
                }
                ContentUriRealPathType.FullPath -> {
                    MyDroidTransferFragment.fileExportSuccessCallback?.invoke("转存到${pair.first}成功！", p)
                }
            }
        } else {
            MyDroidTransferFragment.fileExportFailCallback?.invoke("转存失败！")
        }
    }

    private suspend fun exportInThread2(file: File) {
        val info = exportFileToDownload(file.name , file)
        if (!info.contains("error")) {
            ignoreError { file.delete() }
            logd { "export file info: $info" }
            val splits = info.split("\n")
            MyDroidTransferFragment.fileExportSuccessCallback?.invoke(splits[0], splits[1])
        } else {
            MyDroidTransferFragment.fileExportFailCallback?.invoke(info)
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