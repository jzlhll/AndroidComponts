package com.allan.mydroid.views.textchat

import android.content.res.ColorStateList
import androidx.core.graphics.toColorInt
import com.allan.mydroid.databinding.TextchatItemMeBinding
import com.allan.mydroid.databinding.TextchatItemOtherBinding
import com.allan.mydroid.databinding.TextchatItemStatusNotifyBinding
import com.allan.mydroid.globals.getIcon
import com.allan.mydroid.views.textchat.uibean.AbsItem
import com.allan.mydroid.views.textchat.uibean.NormalItem
import com.allan.mydroid.views.textchat.uibean.StatusItem
import com.au.module_android.utils.gone
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.utils.visible
import com.au.module_nested.recyclerview.viewholder.BindViewHolder

class TextChatRcvHolderStatus(binding: TextchatItemStatusNotifyBinding) : BindViewHolder<AbsItem, TextchatItemStatusNotifyBinding>(binding) {
    override fun bindData(bean: AbsItem) {
        super.bindData(bean)
        bean as StatusItem
        binding.statusTv.text = bean.status
    }
}

class TextChatRcvHolderMe(binding: TextchatItemMeBinding) : BindViewHolder<AbsItem, TextchatItemMeBinding>(binding) {
    override fun bindData(bean: AbsItem) {
        super.bindData(bean)
        bean as NormalItem
        val isServer = bean.message.sender.isServer
        val headIcon = if(isServer) com.allan.mydroid.R.drawable.ic_head_host else com.allan.mydroid.R.drawable.ic_head_client
        binding.icon.setImageResource(headIcon)
        logdNoFile { "colorcolor1 ${bean.message.sender.color}" }
        binding.icon.imageTintList = colorStateListMap(bean.message.sender.color)
        binding.nameTv.text = bean.message.sender.name
        //todo 文件和文字共存
        if (bean.message.content.text.isNotEmpty()) {
            binding.text.text = bean.message.content.text
            binding.text.visible()
        } else {
            binding.text.gone()
        }

        val file = bean.message.content.file
        if (file != null) {
            binding.fileTypeIcon.setImageResource(getIcon(file.name))
            binding.fileNameTv.text = file.name
            binding.fileSizeAndMD5Tv.text = file.fileSizeStr
            binding.fileGroup.visible()
        } else {
            binding.fileGroup.gone()
        }
    }
}

class TextChatRcvHolderOther(binding: TextchatItemOtherBinding) : BindViewHolder<AbsItem, TextchatItemOtherBinding>(binding) {
    override fun bindData(bean: AbsItem) {
        super.bindData(bean)
        bean as NormalItem
        val isServer = bean.message.sender.isServer
        val headIcon = if(isServer) com.allan.mydroid.R.drawable.ic_head_host else com.allan.mydroid.R.drawable.ic_head_client
        binding.icon.setImageResource(headIcon)
        logdNoFile { "colorcolor2 ${bean.message.sender.color}" }
        binding.icon.imageTintList = colorStateListMap(bean.message.sender.color)
        binding.nameTv.text = bean.message.sender.name
        //todo 文件和文字共存
        if (bean.message.content.text.isNotEmpty()) {
            binding.text.text = bean.message.content.text
            binding.text.visible()
        } else {
            binding.text.gone()
        }

        val file = bean.message.content.file
        if (file != null) {
            binding.fileTypeIcon.setImageResource(getIcon(file.name))
            binding.fileNameTv.text = file.name
            binding.fileSizeAndMD5Tv.text = file.fileSizeStr
            binding.fileGroup.visible()
        } else {
            binding.fileGroup.gone()
        }
    }
}

/**
 * key是color的String形式；value是ColorStateList
 */
private val colorStateMap by unsafeLazy { HashMap<String, ColorStateList>() }

private fun colorStateListMap(color:String): ColorStateList {
    val csl = colorStateMap[color]
    if (csl == null) {
        val newCsl = ColorStateList.valueOf(color.toColorInt())
        colorStateMap[color] = newCsl
        return newCsl
    }
    return csl
}