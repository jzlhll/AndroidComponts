package com.au.module_android.click

import android.view.View

/**
 * @author au
 * Date: 2023/8/24
 * Description 有间隔的点击事件
 */
class PaddingClickListener(private val paddingTime:Long?, private val wrapClick:(view: View) ->Unit) : View.OnClickListener {
    override fun onClick(v: View?) {
        if (!acceptClick(paddingTime)) {
            return
        }
        v?.let {
            wrapClick(it)
        }
    }
}

class PaddingTagClickListener(private val paddingTime:Long?, private val tag:Any, private val wrapClick:(view: View, tag:Any) ->Unit) : View.OnClickListener {
    override fun onClick(v: View?) {
        if (!acceptClick(paddingTime)) {
            return
        }
        v?.let {
            wrapClick(it, tag)
        }
    }
}