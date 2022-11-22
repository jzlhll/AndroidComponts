package com.allan.androidlearning.views

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.allan.androidlearning.databinding.CoordinatorRvBinding

//1. 定义Adapter实现类 并 实现3个函数onCreateViewHolder，onBindViewHolder，getItemCount
class MyRecyclerViewAdapter : RecyclerView.Adapter<MyViewHolder>() {
    private val TAG:String = "allan"

    //2. 定义数组和Bean类型
    var list:ArrayList<Bean>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        //忽略viewType
        Log.d(TAG, "onCreateViewHolder: ")
        val cxt = parent.context
        val ll = LinearLayout(cxt).also {
            it.orientation = LinearLayout.VERTICAL
            it.addView(TextView(cxt).also {
                    tv->
                tv.tag = "tv"
            })
            it.addView(Button(cxt).also { btn ->
                btn.tag = "btn" })
        }
        return MyViewHolder(ll)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: position $position")
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
        activity.let {
            val rv = RecyclerView(it)
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
            val view = parent ?: activity.window.decorView
            if (view is ViewGroup) {
                view.addView(rv)
            }
        }
    }

    fun testWithLayout(activity: AppCompatActivity, parent:ViewGroup? = null) {
        activity.let {
            val l = CoordinatorRvBinding.inflate(activity.layoutInflater)
            val rv = l.recyclerview
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
            val view = parent ?: activity.window.decorView
            if (view is ViewGroup) {
                view.addView(l.root)
            }
        }
    }
}