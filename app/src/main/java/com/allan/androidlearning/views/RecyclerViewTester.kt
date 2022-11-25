package com.allan.androidlearning.views

import android.graphics.Color
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginLeft
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

//1. 定义Adapter实现类 并 实现3个函数onCreateViewHolder，onBindViewHolder，getItemCount
class MyRecyclerViewAdapter : RecyclerView.Adapter<MyViewHolder>() {
    private val TAG:String = "allan"

    //2. 定义数组和Bean类型
    var list:ArrayList<Bean>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        //忽略viewType
        val cxt = parent.context
        val ll = LinearLayout(cxt).also {
            it.orientation = LinearLayout.HORIZONTAL
            it.addView(TextView(cxt).also {
                    tv->
                tv.tag = "tv"
                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                lp.marginStart = 40
                tv.layoutParams = lp
            })
            it.addView(Button(cxt).also { btn ->
                btn.tag = "btn" })
            it.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT)
        }
        return MyViewHolder(ll)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val bean = list?.get(position)
        bean?.let {
            holder.tv.text = bean.name
            holder.btn.text = bean.age.toString()
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onViewAttachedToWindow(holder: MyViewHolder) {
        super.onViewAttachedToWindow(holder)
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

}


//2.我们需要给Adapter设置泛型对象，即ViewHolder。很简单只需要传入和做记录即可
class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val btn:Button
    val tv:TextView

    init {
        btn = itemView.findViewWithTag("btn")
        tv = itemView.findViewWithTag("tv")
    }
}

class Bean(var age:Int, var name:String) {
}

class RecyclerViewTester {
    fun test(activity: AppCompatActivity, parent:ViewGroup? = null) {
        test(activity, parent)
    }

    fun test(activity: AppCompatActivity, rcv:RecyclerView?, parent:ViewGroup?) {
        activity.let {
            val rv = rcv ?: RecyclerView(it)
            rv.addItemDecoration(PaddingItemDecoration(30, 20, true))
            //3. 别忘记设置layoutManager
            rv.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
            //rv.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            val adapter = MyRecyclerViewAdapter()
            adapter.list = ArrayList<Bean>().also { list->
                var i = 0
                while(i++ < 100) {
                    list.add(Bean(i, "Bob$i"))
                }
            }

            rv.adapter = adapter
            if (rv.parent == null) {
                val view = parent ?: activity.window.decorView
                if (view is ViewGroup) {
                    view.addView(rv)
                }
            }
        }
    }
}


/**
 * @author: Qiu sj
 * @date: 2022/9/19 10:38
 * @description:
 */
class PaddingItemDecoration : RecyclerView.ItemDecoration {
    private var padding: Int
    private var spacing: Int
    private var isVertical: Boolean

    constructor(padding: Int, spacing: Int, isVertical: Boolean) {
        this.padding = padding
        this.spacing = spacing
        this.isVertical = isVertical
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {

        val position = parent.getChildAdapterPosition(view) // item position
        val count = parent.adapter?.itemCount ?: 0
        if (isVertical) {
            when (position) {
                0 -> {
                    outRect.top = padding
                }
                count - 1 -> {
                    outRect.top = spacing
                    outRect.bottom = padding
                }
                else -> {
                    outRect.top = spacing
                }
            }
        } else {
            when (position) {
                0 -> {
                    outRect.left = padding
                }
                count - 1 -> {
                    outRect.left = spacing
                    outRect.right = padding
                }
                else -> {
                    outRect.left = spacing
                }
            }
        }
    }
}