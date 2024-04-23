package com.au.module_android.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.au.module_android.ui.views.ViewActivity

open class NormalViewActivity : ViewActivity() {
    companion object {
        private const val KEY_INTENT_MASK = "intent_mask_str"
        private const val KEY_INTENT_MASK2 = "intent_mask_2"
        private val actionMap = hashMapOf<String, (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)->ViewBinding>()

        fun Context.start(maskStr:String, viewBindingSupply:(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)->ViewBinding) {
            val mask2 = viewBindingSupply.toString()
            actionMap[maskStr + "_" + mask2] = viewBindingSupply

            startActivity(Intent(this, NormalViewActivity::class.java).also {
                it.putExtra(KEY_INTENT_MASK, maskStr)
                it.putExtra(KEY_INTENT_MASK2, mask2)
            })
        }
    }

    override fun onUiCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val mask = intent.getStringExtra(KEY_INTENT_MASK)
        val mask2 = intent.getStringExtra(KEY_INTENT_MASK2)
        val supply = actionMap.remove(mask + "_" + mask2) ?: throw RuntimeException("NormalViewActivity has no mask.")
        val vb = supply.invoke(inflater, container, savedInstanceState)
        return vb.root
    }
}