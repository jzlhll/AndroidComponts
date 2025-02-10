package com.au.module_nested.viewpager2

import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.au.module_android.utils.ReflectionUtils
import com.au.module_android.utils.asOrNull
import java.lang.Integer.min
import java.util.concurrent.atomic.AtomicLong

/**
 * @author allan.jiang
 * Date: 2023/6/29
 * Description 用于需要更新Fragment的ViewPager2的Adapter。给你的Fragment实现IFragmentNeedUpdate，
 * 会在生命周期onStart自动回调或者有动态更新的时候通知。因此，不需要编写data和onCreateView的代码。
 */
@Keep
class NeedUpdateFragmentStateAdapter<T>(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private var datas: ArrayList<T> = ArrayList()
    //与datas对应的Id
    private var myIds:ArrayList<Long> = ArrayList()

    private val idGen:AtomicLong = AtomicLong(0)
    private val nextId
        get() = idGen.incrementAndGet()

    /**
     * 参数 新增的Fragment是否是插入到最后。
     */
    var insertAtEnd = true

    //如果你的是android.util.LongSparseArray自行调整。
    private var mFragmentsParent:androidx.collection.LongSparseArray<Fragment>? = null
    private fun mFragmentsRequire() : androidx.collection.LongSparseArray<Fragment>? {
        var mFragments = mFragmentsParent
        if (mFragments == null) {
            mFragments = ReflectionUtils.iteratorGetPrivateFieldValue(this, "mFragments", true).asOrNull<androidx.collection.LongSparseArray<Fragment>>()
            mFragmentsParent = mFragments
            return mFragments
        }
        return mFragments
    }

    /**
     * 数据比较器。
     * 由于比较的是Fragment，因此，如果Fragment的主要属性不变化则不得变化。
     * 然后通过局部更新的方式来更新Fragment里面的内容
     *
     * true表示确实是有区别的需要重建fragment, 如果是false则不会重建并会通知到onNeedUpdate函数。
     */
    var differComparator:((d1:T, d2:T)->Boolean)? = null

    /**
     * Fragment构建器。根据data来创建。
     * 请创建出Fragment，实现IFragmentNeedUpdate。
     */
    var fragmentCreator:((data:T)-> IFragmentNeedUpdate<T>)? = null

    fun submitData(newDatas:List<T>) {
        if (fragmentCreator == null) {
            throw RuntimeException("you have not set fragment Creator.")
        }
        if (differComparator == null) {
            throw RuntimeException("you have not set differ Comparator.")
        }

        val mFragments = mFragmentsRequire()
        if (idGen.get() == 0L || mFragments == null) {
            initData(newDatas)
        } else {
            updateData(newDatas, mFragments)
        }
    }

    /**
     * 初始化数据
     */
    private fun initData(datas:List<T>) {
        this.datas.clear()
        this.myIds.clear()
        this.datas.addAll(datas)
        var sz = datas.size
        while (sz-->0) {
            myIds.add(nextId)
        }

        notifyDataSetChanged()
    }

    //完全按照新列表，顺序来显示。
    private fun updateData(newDatas:List<T>, mFragments:androidx.collection.LongSparseArray<Fragment>) {
        val minSize = min(datas.size, newDatas.size)
        val deltaSize = kotlin.math.abs(datas.size - newDatas.size)
        //更新前面的Fragment
        for (i in 0 until minSize) {
            val newData = newDatas[i]
            if (differComparator?.invoke(datas[i], newData) == true) {
                myIds[i] = nextId //更新id。放到后面，否则提取不匹配。
            } else {
                //现在就存在的Fragment，我们需要更新它
                //先拿到老id进行更新
                val ifg = mFragments.get(myIds[i]).asOrNull<IFragmentNeedUpdate<T>>()
                val fg = ifg.asOrNull<Fragment>()
                if(ifg != null && fg != null && fg.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) ifg.onNeedUpdate(newData)
            }
            datas[i] = newData //不论如何都要换新数据的。
        }

        //处理后面的数据
        if (newDatas.size < datas.size) {
            var delta = deltaSize
            while(delta-- > 0) {
                datas.removeAt(datas.lastIndex)
                myIds.removeAt(myIds.lastIndex)
            }
            notifyItemRangeRemoved(minSize, deltaSize)
        } else if (newDatas.size > datas.size) {
            if (insertAtEnd) {
                var delta = deltaSize
                var min = minSize
                while (delta-- > 0) {
                    datas.add(newDatas[min++])
                    myIds.add(nextId)
                }
                notifyItemRangeInserted(minSize, deltaSize)
            } else {
                var delta = deltaSize
                var min = minSize
                while (delta-- > 0) {
                    datas.add(0, newDatas[min++])
                    myIds.add(0, nextId)
                }
                notifyItemRangeInserted(0, deltaSize)
            }
        }
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun createFragment(position: Int): Fragment {
        val data = datas[position]
        val fc = fragmentCreator?.invoke(data)
        val fragment = fc as Fragment
        fragment.lifecycle.addObserver(DataLifecycleObserver(data))
        return fragment
    }

    override fun containsItem(itemId: Long): Boolean {
        return myIds.contains(itemId)
    }

    override fun getItemId(position: Int): Long {
        return myIds[position]
    }

    /**
     * 请将你的Fragment实现它。将会通知你更新
     */
    interface IFragmentNeedUpdate<T> {
        fun onNeedUpdate(data:T)
    }

    class DataLifecycleObserver<T>(private val data:T) : DefaultLifecycleObserver {
        private var isInit = false

        override fun onStart(owner: LifecycleOwner) {
            if (!isInit && owner is IFragmentNeedUpdate<*>) {
                isInit = true
                val f = owner as IFragmentNeedUpdate<T>
                f.onNeedUpdate(data)
            }
        }
    }

}