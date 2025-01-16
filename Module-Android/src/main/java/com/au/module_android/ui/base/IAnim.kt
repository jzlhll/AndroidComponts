package com.au.module_android.ui.base

import androidx.annotation.AnimRes

interface IAnim {
    @get:AnimRes
    val enterAnim:Int?

    @get:AnimRes
    val exitAnim:Int?
}