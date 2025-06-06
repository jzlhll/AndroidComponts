package com.allan.mydroid.views

import android.os.Bundle
import com.allan.mydroid.databinding.FragmentMyDroidTextChatBinding

class TextChatFragment : AbsTransferAliveFragment<FragmentMyDroidTextChatBinding>() {
    override fun onBindingCreated(savedInstanceState: Bundle?) {
        super.onBindingCreated(savedInstanceState)

        val fmt = getString(com.allan.mydroid.R.string.not_close_window)
        binding.descTitle.text = String.format(fmt, "")
    }
}