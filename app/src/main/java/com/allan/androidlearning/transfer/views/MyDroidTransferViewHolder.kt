package com.allan.androidlearning.transfer.views

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.core.os.bundleOf
import com.allan.androidlearning.R
import com.allan.androidlearning.databinding.HolderMydroidFileitemBinding
import com.allan.androidlearning.transfer.benas.MergedFileInfo
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.utilsmedia.MediaHelper
import com.au.module_android.utilsmedia.shareFile
import com.au.module_androidui.dialogs.FragmentBottomSheetDialog
import com.au.module_nested.recyclerview.viewholder.BindViewHolder

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
            "doc", "docx" -> R.drawable.ic_filetype_doc
            "xls", "xlsx" -> R.drawable.ic_filetype_xls
            "pdf" -> R.drawable.ic_filetype_pdf
            "txt", "log", "md" -> R.drawable.ic_filetype_text
            // 压缩包类型
            "zip", "rar", "tar", "gz", "7z" -> R.drawable.ic_filetype_archive
            // 代码文件类型（可选扩展）
            "java", "kt", "py", "js", "html", "css" -> R.drawable.ic_filetype_code
            // 其他类型
            else ->
                if (MediaHelper.isImageFileSimple(extension)) {
                    R.drawable.ic_filetype_image
                } else if (MediaHelper.isAudioFileSimple(extension)) {
                    R.drawable.ic_filetype_audio
                } else if (MediaHelper.isVideoFileSimple(extension)) {
                    R.drawable.ic_filetype_video
                } else {
                    R.drawable.ic_filetype_other
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