package com.au.audiorecordplayer.cam2

import android.os.Bundle
import android.os.HandlerThread
import android.os.Message
import com.au.audiorecordplayer.cam2.impl.MyCamManager
import com.au.audiorecordplayer.databinding.FragmentCamera2Binding
import com.au.module_android.ui.bindings.BindingFragment

class Camera2Fragment : BindingFragment<FragmentCamera2Binding>() {
    private var camManager:MyCamManager? = null
    private var mSubThread: HandlerThread? = null

    override fun onBindingCreated(savedInstanceState: Bundle?) {
    }

    override fun onStart() {
        super.onStart()
        val subThread = HandlerThread("Camera-thread")
        mSubThread = subThread
        subThread.start()
        camManager = MyCamManager(binding.previewView,
            MyCamManager.TRANSMIT_TO_MODE_PREVIEW,
            subThread.looper)
    }

    override fun onStop() {
        super.onStop()
        camManager?.removeCallbacksAndMessages(null)
        camManager?.destroy()
        mSubThread?.quit()
    }



    fun setPreviewSize(width: Int, height: Int) {
        binding.previewView.setAspectRatio((width * 1.0f / height).toDouble())
    }
}