package com.allan.androidlearning.transfer

import android.view.ViewGroup
import com.allan.androidlearning.activities2.MyDroidTransferFragment
import com.allan.androidlearning.databinding.HolderMydroidFileitemBinding
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.utils.MediaHelper
import com.au.module_android.utils.exportFileToDownload
import com.au.module_android.utils.ignoreError
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logd
import com.au.module_nested.recyclerview.BindRcvAdapter
import com.au.module_nested.recyclerview.viewholder.BindViewHolder

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
        }

        binding.sharesBtn.onClick {
            val d = currentData ?: return@onClick
            MediaHelper().shareFile(Globals.app, d.file)
        }
    }

    fun getIcon(fileName: String): String {
        // 提取文件后缀并转为小写（处理无后缀的情况）
        val extension = fileName.substringAfterLast('.', "").lowercase()

        return when (extension) {
            // 文本/文档类型
            "txt", "doc", "docx", "pdf", "md", "rtf", "odt" -> "text"
            // 图片类型
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg" -> "image"
            // 视频类型
            "mp4", "avi", "mov", "mkv", "flv", "wmv" -> "video"
            // 音频类型
            "mp3", "wav", "ogg", "flac", "aac", "m4a" -> "audio"
            // 压缩包类型
            "zip", "rar", "tar", "gz", "7z" -> "archive"
            // 代码文件类型（可选扩展）
            "java", "kt", "py", "js", "html", "css" -> "code"
            // 其他类型
            else -> "other"
        }
    }

    override fun bindData(bean: MergedFileInfo) {
        super.bindData(bean)
        binding.fileNameTv.text = bean.file.name
        binding.fileSizeTv.text = bean.fileSizeInfo
        binding.md5Tv.text = bean.md5
    }
}