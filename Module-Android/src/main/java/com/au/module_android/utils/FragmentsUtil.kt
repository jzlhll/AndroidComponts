package com.au.module_android.utils

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * 换一个Fragment
 */
fun AppCompatActivity.replaceFragment(@IdRes fragmentContainerViewId: Int, fragment:Fragment) {
    supportFragmentManager.beginTransaction().replace(fragmentContainerViewId, fragment).commitAllowingStateLoss()
}

/**
 * 换一个Fragment
 */
fun Fragment.replaceFragment(@IdRes fragmentContainerViewId: Int, fragment:Fragment) {
    childFragmentManager.beginTransaction().replace(fragmentContainerViewId, fragment).commitAllowingStateLoss()
}

/**
 * 移除一个Fragment
 */
fun AppCompatActivity.removeFragment(fragment:Fragment) {
    supportFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
}

/**
 * 移除一个Fragment
 */
fun Fragment.removeFragment(fragment:Fragment) {
    childFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
}

