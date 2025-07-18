package com.allan.mydroid.views.textchat

import android.net.Uri
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.allan.mydroid.beans.WSChatMessageBean
import com.allan.mydroid.beansinner.UriRealInfoEx
import com.allan.mydroid.beansinner.UriRealInfoHtml
import com.allan.mydroid.databinding.FragmentTextChatBinding
import com.allan.mydroid.views.send.SendListSelectorDialog
import com.allan.mydroid.views.textchat.uibean.AbsItem
import com.allan.mydroid.views.textchat.uibean.NormalItem
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.utils.ImeHelper
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.getScreenFullSize
import com.au.module_android.utils.logd
import com.au.module_android.utils.setMaxLength
import com.au.module_android.utils.transparentStatusBar
import com.au.module_android.utilsmedia.getRealInfo
import com.au.module_androidui.dialogs.FragmentBottomSheetDialog
import com.au.module_imagecompressed.MultiPhotoPickerContractResult
import com.au.module_imagecompressed.TakeAndSelectMediaPermissionHelper
import kotlinx.coroutines.launch

abstract class TextChatCommon(val f: Fragment, val binding: FragmentTextChatBinding) {
    private lateinit var adapter: TextChatRcvAdapter

    private fun cvtUri(uri: Uri): UriRealInfoHtml {
        val real = uri.getRealInfo(Globals.app)
        if (real.goodPath() == null) {
            logd { "allan no good path? $real" }
        }
        val bean = UriRealInfoEx.Companion.copyFrom(real)
        return bean.copyToHtml()
    }

    private val photoVideoPicker = TakeAndSelectMediaPermissionHelper(f,
        com.allan.mydroid.BuildConfig.APPLICATION_ID,
        1,
        pickerType = MultiPhotoPickerContractResult.PickerType.IMAGE_AND_VIDEO).also {
        it.allResultsAction = { results->
            logd { "allan photoVideoPicker $results" }
            for (uri in results) {
                val uriRealInfoHtml = cvtUri(uri.uri)
                buttonSend(createBean(WSChatMessageBean.Content("", uriRealInfoHtml)))
            }
        }
    }

    fun onCreate() {
        binding.edit.setMaxLength(Int.MAX_VALUE)

        val ac = f.requireActivity()
        val helper = ImeHelper.assist(ac)
        helper?.setOnImeListener { imeOffset: Int,
                                   imeMaxHeight: Int/*包含导航栏和状态栏总高度*/,
                                   statusBarHeight: Int,
                                   navigationBarHeight: Int ->
            //imeOffset 1087 imeMaxH 1090 stH 122 navH 0
            binding.bottomHost.translationY = -imeOffset.toFloat()
        }
        helper?.setOnImeMaxHeightListener { imeMaxHeight->
            //imeOffset 1087 imeMaxH 1090 stH 122 navH 0
            binding.bottomHost.translationY = -imeMaxHeight.toFloat()
        }

        ac.transparentStatusBar(statusBarTextDark = false) { insets, statusBarsHeight, _ ->
            binding.toolbar.layoutParams.asOrNull<ConstraintLayout.LayoutParams>()?.let { toolbarLP->
                toolbarLP.topMargin = statusBarsHeight
                binding.toolbar.layoutParams = toolbarLP
            }
            insets
        }

        initSendButton()

        initRcv()

        binding.selectImagesBtn.onClick {
            photoVideoPicker.onClickSelectPhoto()
        }

        binding.selectVideoBtn.onClick {
            photoVideoPicker.onClickSelectPhoto()
        }
        binding.sendListBtn.onClick {
            val height = f.requireActivity().getScreenFullSize().second
            FragmentBottomSheetDialog.show<SendListSelectorDialog>(f.childFragmentManager, height = height / 2)
        }
    }

    abstract fun buttonSend(bean : WSChatMessageBean)

    abstract fun createBean(content: WSChatMessageBean.Content) : WSChatMessageBean

    /**
     * 当发送后，即可将该条bean添加显示
     */
    fun onAddChatItem(newItem: AbsItem) {
        f.lifecycleScope.launch {
            var foundIndex = -1
            //找到之前的发送中的item。todo ui上的转圈变化。
            if (newItem is NormalItem && newItem.isMe) {
                run loop@ {
                    adapter.datas.forEachIndexed { index, item ->
                        if (item is NormalItem && item.isMe && item.message.messageId == newItem.message.messageId) {
                            foundIndex = index
                            return@loop
                        } else {
                            false
                        }
                    }
                }
            }

            if (foundIndex >= 0) {
                adapter.updateItem(foundIndex, newItem)
            } else {
                adapter.addItem(newItem)
            }
        }
    }

    private fun initRcv() {
        binding.rcv.adapter = TextChatRcvAdapter{

        }.also { adapter = it }
        binding.rcv.layoutManager = LinearLayoutManager(f.requireContext()).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        binding.rcv.setHasFixedSize(false)
        binding.rcv.itemAnimator = null
    }

    private fun initSendButton() {
        binding.sendBtn.onClick {
            val text = binding.edit.text.toString()
            if (text.isNotEmpty()) {
                binding.edit.clearFocus()
                binding.edit.setText("")
                buttonSend(createBean(WSChatMessageBean.Content(text, null))) //todo file
            }
        }
    }

    fun selectPics() {

    }

    fun selectVideos() {

    }

    fun selectFiles() {

    }

    fun showImportSendList() {

    }
}