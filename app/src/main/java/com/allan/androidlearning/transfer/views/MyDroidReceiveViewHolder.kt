package com.allan.androidlearning.transfer.views

import android.annotation.SuppressLint
import androidx.core.os.bundleOf
import com.allan.androidlearning.databinding.HolderMydroidFileitemBinding
import com.allan.androidlearning.transfer.benas.MergedFileInfo
import com.allan.androidlearning.transfer.getIcon
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.utilsmedia.shareFile
import com.au.module_androidui.dialogs.FragmentBottomSheetDialog
import com.au.module_nested.recyclerview.viewholder.BindViewHolder

class MyDroidReceiveViewHolder(binding: HolderMydroidFileitemBinding) : BindViewHolder<MergedFileInfo, HolderMydroidFileitemBinding>(binding) {
    init {
        binding.downloadBtn.onClick {
            val d = currentData ?: return@onClick
            val file = d.file
            val fragmentMgr = (bindingAdapter as MyDroidReceiveFileListAdapter).f.childFragmentManager

            FragmentBottomSheetDialog.show<ExportSelectActionDialog>(fragmentMgr, bundleOf("file" to file))
        }

        binding.sharesBtn.onClick {
            val d = currentData ?: return@onClick
            shareFile(Globals.app, d.file)
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