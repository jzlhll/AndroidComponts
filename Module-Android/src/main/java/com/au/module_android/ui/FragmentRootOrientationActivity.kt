package com.au.module_android.ui

import android.content.Context
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.au.module_android.permissions.activity.ActivityForResult

/**
 * @author au
 * Date: 2023/8/29
 * Description 限制
 */
class FragmentRootOrientationActivity : FragmentRootActivity() {
    companion object {
        /**
         * 把一个Fragment放到本Activity当做唯一的界面。
         *
         * @param context Context
         * @param fragmentClass 需要显示的fragment的类
         * @param activityResult 如果传入了非空对象，则会通过它启动，会携带返回；否则就是默认启动。
         * @param arguments 用来透传给Fragment
         * @param optionsCompat 是startActivity的参数
         */
        fun start(context: Context,
                            fragmentClass:Class<out Fragment>,
                            activityResult:ActivityForResult? = null,
                            arguments: Bundle? = null,
                            optionsCompat: ActivityOptionsCompat? = null,
                            hasWebView:Boolean = false,
                            autoHideIme:Boolean = false)  {
            start(context, FragmentRootOrientationActivity::class.java, fragmentClass, activityResult, arguments, optionsCompat, hasWebView, autoHideIme)
        }
    }
}