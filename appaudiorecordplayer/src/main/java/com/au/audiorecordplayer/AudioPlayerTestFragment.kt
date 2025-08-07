package com.au.audiorecordplayer

import android.os.Bundle
import com.au.audiorecordplayer.databinding.AudioPlaysBinding
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.ui.views.ToolbarInfo

class AudioPlayerTestFragment : BindingFragment<AudioPlaysBinding>() {
    override fun onBindingCreated(savedInstanceState: Bundle?) {
    }

    override fun toolbarInfo(): ToolbarInfo? {
        return ToolbarInfo("AudioPlayers")
    }
}