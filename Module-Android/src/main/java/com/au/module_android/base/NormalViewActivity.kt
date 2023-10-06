package com.au.module_android.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.au.module_android.arct.BaseViewActivity

class NormalViewActivity : BaseViewActivity() {
    override fun onCommonCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val xmlId = intent.getIntExtra(KEY_XML_LAYOUT_ID, 0)
        return inflater.inflate(xmlId, container)
    }
}