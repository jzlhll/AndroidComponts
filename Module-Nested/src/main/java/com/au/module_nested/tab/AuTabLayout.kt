package com.au.module_nested.tab

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.au.module_android.fontutil.setFontFromAsset
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.forEachChild
import com.au.module_android.widget.CustomFontText
import com.au.module_android.widget.FontMode
import com.au.module_androidcolor.R
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

    /**
     * 使用CustomFontText来作为customview设置给TabLayout。
     * 由于默认情况，TabLayout的文字都是material效果。故而采用customView来解决文案的颜色和字体。简单一点。
     */
    fun initAttachToViewPage2AsCustomFontText(viewPage2: ViewPager2, pages:List<Pair<String, Class<out Fragment>>>) {
        TabLayoutMediator(this, viewPage2, true) { tab: Tab, position: Int ->
            val tv = CustomFontText(viewPage2.context)
            tv.gravity = Gravity.CENTER
            tv.fontMode = FontMode.MID
            tv.text = pages[position].first
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            val colorId = if (viewPage2.currentItem == position)
                R.color.color_tab_text_select
            else
                R.color.color_tab_text_no_select

            tv.setTextColor(ContextCompat.getColor(tv.context, colorId))
            tab.customView = tv
        }.attach()

        initSelectedListener()
    }

    fun initSelectedListener() {
        addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: Tab?) {
                val v = tab?.customView.asOrNull<TextView>()
                v?.setTextColor(ContextCompat.getColor(v.context, R.color.color_tab_text_select))
            }

            override fun onTabUnselected(tab: Tab?) {
                val v = tab?.customView.asOrNull<TextView>()
                v?.setTextColor(ContextCompat.getColor(v.context, R.color.color_tab_text_no_select))
            }

            override fun onTabReselected(tab: Tab?) {
            }
        })
    }

    fun newTextTab(text:String, initSelect:Boolean, fontSize:Float = 16f) : Tab {
        val tab = super.newTab()
        val tv = CustomFontText(context)
        tv.gravity = Gravity.CENTER
        tv.fontMode = FontMode.MID
        tv.text = text
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
        val colorId = if (initSelect)
            R.color.color_tab_text_select
        else
            R.color.color_tab_text_no_select

        tv.setTextColor(ContextCompat.getColor(tv.context, colorId))
        tab.customView = tv
        return tab
    }
}