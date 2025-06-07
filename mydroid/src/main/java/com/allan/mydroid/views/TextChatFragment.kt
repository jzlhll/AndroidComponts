package com.allan.mydroid.views

import android.os.Bundle
import com.allan.mydroid.databinding.FragmentTextChatServerBinding

class TextChatFragment : AbsLiveFragment<FragmentTextChatServerBinding>() {
    override fun onBindingCreated(savedInstanceState: Bundle?) {
        super.onBindingCreated(savedInstanceState)

        val fmt = getString(com.allan.mydroid.R.string.not_close_window)
        binding.descTitle.text = String.format(fmt, "")
    }
}