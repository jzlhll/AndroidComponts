package com.allan.nested.recyclerview

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.au.module_android.postToMainHandler

/**
 * @author au
 * @date :2023/11/6 11:31
 * @description: 如果是静态数据的RecyclerView，setAdapter需要额外处理。
 * 避免出现问题。
 */
class StaticRecyclerView : RecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    @Volatile
    private var isDrawn = -1

    @Volatile
    private var initedAdapter = false

    private var mBeWrapAdapter:Adapter<*>? = null

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        if(isDrawn == -1) isDrawn = 0
        postSetAdapter()
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        mBeWrapAdapter = adapter
        postSetAdapter()
    }

    private fun postSetAdapter() { //不论如何，都将在onDraw之后，再post。比之前的之前设置更为保险
        if (isDrawn == 0) {
            isDrawn = 1
            postToMainHandler {
                if (mBeWrapAdapter == null) {
                    Log.e("allan", "please setBottomNavigateAdapter earlier!")
                }
                if (!initedAdapter) {
                    super.setAdapter(mBeWrapAdapter)
                    initedAdapter = true
                }
            }
        }
    }
}