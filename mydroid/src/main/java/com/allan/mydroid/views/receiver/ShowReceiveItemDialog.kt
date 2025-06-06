package com.allan.mydroid.views.receiver

import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.allan.mydroid.databinding.ShowReceiveItemInfosBinding
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.serializableCompat
import com.au.module_androidui.dialogs.FragmentBottomSheetDialog

class ShowReceiveItemDialog : BindingFragment<ShowReceiveItemInfosBinding>() {
    companion object {
        fun pop(manager: FragmentManager,
                texts:Array<String>) {
            assert(texts.size >= 3)
            FragmentBottomSheetDialog.show<ShowReceiveItemDialog>(manager, bundleOf("texts" to texts))
        }
    }

    override fun onStart() {
        super.onStart()
        val texts = arguments?.serializableCompat<Array<String>>("texts")
        val text1 = texts?.get(0)
        val text2 = texts?.get(1)
        val text3 = texts?.get(2)

        binding.text1.text = text1
        binding.text2.text = text2
        binding.text3.text = text3
    }
}