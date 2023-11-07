package com.allan.nested.bottom_nav

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewbinding.ViewBinding
import androidx.viewbinding.ViewBindings
import androidx.viewpager2.widget.ViewPager2
import com.allan.nested.anim.AnimationUtil
import com.allan.nested.recyclerview.BindRcvAdapter
import com.allan.nested.recyclerview.viewholder.BindViewHolder

/**
 * @author allan
 * @date :2023/11/6 14:51
 * @description:
 */
class BottomNavAdapter<VB:ViewBinding>(data:List<BottomPageBean>, private val viewBindingClazz:Class<out ViewBinding>)
    : BindRcvAdapter<BottomPageBean, BottomNavHolder<VB>>() {

    private lateinit var itemViewChangeFun:((VB, BottomPageBean)->Unit)

    /**
     *  动画的目标
     */
    private var switchBtnAnimObjectApply:((VB)->View)? = null

    init {
        this.datas.addAll(data)
    }

    /**
     * 绑定上viewPager2。跟随变动。
     * 只调用一次。
     * @param itemViewChangeFun 是用来当itemHolder需要显示和刷新的时候，给出根据bean来显示的模式。
     * @param switchBtnAnimObjectFun 如果传入了，则会有动画。没有传入，则代表不需要动画。
     */
   fun bindWithBottomNav(viewPager2:ViewPager2,
                         itemViewChangeFun:((VB, BottomPageBean)->Unit),
                         switchBtnAnimObjectApply:((VB)->View)? = null) {
        this.itemViewChangeFun = itemViewChangeFun
        this.switchBtnAnimObjectApply = switchBtnAnimObjectApply

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val changedList = ArrayList<Int>()
                for (i in 0 until datas.size) {
                    val old = datas[i].isSelected
                    val new = position == i
                    datas[i].isSelected = new
                    if (old != new) {
                        changedList.add(i)
                    }
                }
                changedList.forEach {
                    this@BottomNavAdapter.notifyItemChanged(it)
                }
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomNavHolder<VB> {
        val childWidth = parent.width / datas.count()
        val lp = ViewGroup.LayoutParams(childWidth, ViewGroup.LayoutParams.MATCH_PARENT)
        val itemView = create<VB>(viewBindingClazz, parent)
        itemView.root.layoutParams = lp
        return BottomNavHolder(itemView)
    }

    override fun onBindViewHolder(holder: BottomNavHolder<VB>, position: Int) {
        val b = this.datas[position]
        holder.bindData(b)
        itemViewChangeFun.invoke(holder.binding as VB, b)

        val apply = this.switchBtnAnimObjectApply
        if (apply != null) {
            AnimationUtil().bottomSwitchBtnAnim(apply(holder.binding)).start() //todo 可能考虑第一次默认选中问题会有动画
        }
    }

    override fun getItemCount(): Int {
        return datas.count()
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }
}

class BottomNavHolder<VB:ViewBinding>(itemView:VB) : BindViewHolder<BottomPageBean, ViewBinding>(itemView)