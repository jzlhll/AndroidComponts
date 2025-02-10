package com.au.module_nested.viewpager2

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

fun <T> ViewPager2.simplePagerAdapter(
    activity: AppCompatActivity,
    datas: List<T>,
    onCreateItem: Function2<@ParameterName("position") Int, T, Fragment>
): FragmentStateAdapter {
    return simplePagerAdapter(
        activity.supportFragmentManager,
        activity.lifecycle,
        datas,
        onCreateItem
    )
}

fun <T> ViewPager2.simplePagerAdapter(
    fragment: Fragment,
    datas: List<T>,
    onCreateItem: Function2<@ParameterName("position") Int, T, Fragment>
): FragmentStateAdapter {
    return simplePagerAdapter(
        fragment.childFragmentManager,
        fragment.lifecycle,
        datas,
        onCreateItem
    )
}

fun <T> ViewPager2.simplePagerAdapter(
    fm: FragmentManager,
    lifecycle: Lifecycle,
    datas: List<T>,
    onCreateItem: Function2<@ParameterName("position") Int, T, Fragment>
): FragmentStateAdapter {
    val baseAdapter = object : FragmentStateAdapter(fm, lifecycle) {
        override fun getItemCount(): Int = datas.count()

        override fun createFragment(position: Int): Fragment {
            return onCreateItem.invoke(position, datas[position])
        }
    }
    adapter = baseAdapter
    return baseAdapter
}