package com.allan.nested.tab

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.au.module_android.widget.FontMode
import com.au.module_android.fontutil.setFontFromAsset
import com.au.module_android.utils.forEachChild
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * @author au
 * @date :2023/11/9 13:57
 * @description:
 */
class AuTabLayout : TabLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    inline fun attachViewPager2(
        viewPage2: ViewPager2,
        autoRefresh: Boolean = true,
        crossinline tabBlock: ((tab: Tab, position: Int) -> Unit) = { _, _ -> }
    ) {
        TabLayoutMediator(this, viewPage2, autoRefresh) { tab: Tab, position: Int ->
            tabBlock.invoke(tab, position)
        }.attach()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setFont()
    }

    var isBlod = false

    /**
     * 设置字体样式
     */
    private fun setFont() {
        val context = context
        forEachChild {
            if (it is TextView) {
                if (isBlod){
                    it.setFontFromAsset(context, FontMode.BOLD, false)
                }else{
                    it.setFontFromAsset(context, FontMode.NORMAL, false)
                }
            }
        }
    }
}