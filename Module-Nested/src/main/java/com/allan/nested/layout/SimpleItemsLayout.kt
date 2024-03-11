package com.allan.nested.layout

import android.content.Context
import android.graphics.text.LineBreaker
import android.os.Build
import android.text.Spanned
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.viewbinding.ViewBinding
import com.au.module_android.utils.dp
import com.au.module_android.utils.unsafeLazy

/**
 * @author au
 * @date :2023/11/9 15:29
 * @description: 其实类似RecyclerView，ListView 不需要回收机制的简易，几条items的layout.
 */
class SimpleItemsLayout : LinearLayout {
    //每一行的上下间距
    private val itemMarginTop:Int by unsafeLazy { context.dp(4f).toInt() }

    private val inflater: LayoutInflater

    //请务必初始化。
    lateinit var itemInflateCreator : ((layoutInflate:LayoutInflater, me:SimpleItemsLayout, attachedToParent:Boolean, data:Any)->ViewBinding)

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        inflater = LayoutInflater.from(context)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        inflater = LayoutInflater.from(context)
    }

    fun addItem(data:Any) {
        val viewBinding = itemInflateCreator.invoke(inflater, this, false, data)
        viewBinding.root.tag = data.toString()
        addView(viewBinding.root, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).also {
            it.setMargins(0, itemMarginTop, 0, 0)
        })
    }

    fun removeItem(data:Any) {
        removeView(findViewWithTag(data.toString()))
    }
}