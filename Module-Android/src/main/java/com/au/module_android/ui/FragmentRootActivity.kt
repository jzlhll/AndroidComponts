package com.au.module_android.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.au.module.android.BuildConfig
import com.au.module_android.APP_TAG
import com.au.module_android.permissions.activity.IActivityResult
import com.au.module_android.ui.views.ViewActivity

/**
 * @author allan
 * Date: 2023/8/29
 * Description 限制
 */
class FragmentRootActivity : ViewActivity() {
    companion object {
        const val KEY_FRAGMENT_CLASS = "FragmentRootActivity_key_fragment"
        const val KEY_FRAGMENT_ARGUMENTS = "FragmentRootActivity_key_arguments"

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
                            activityResult:IActivityResult? = null,
                            arguments: Bundle? = null,
                            optionsCompat: ActivityOptionsCompat? = null)  {
            val intent = Intent(context, FragmentRootActivity::class.java)
            intent.putExtra(KEY_FRAGMENT_CLASS, fragmentClass)
            if (arguments != null) intent.putExtra(KEY_FRAGMENT_ARGUMENTS, arguments)

            if (activityResult != null) {
                activityResult.start(intent, optionsCompat)
            } else {
                ActivityCompat.startActivity(context, intent, optionsCompat?.toBundle())
            }
        }
    }

    override fun onCreatingView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = FragmentContainerView(inflater.context)
        v.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        v.id = View.generateViewId()
        val fragmentClass = intent.getSerializableExtra(KEY_FRAGMENT_CLASS) as Class<Fragment>
        val instance = fragmentClass.getDeclaredConstructor().newInstance()
        instance.arguments = intent.getBundleExtra(KEY_FRAGMENT_ARGUMENTS)
        if (BuildConfig.DEBUG) {
            Log.d(APP_TAG, "FragmentRootActivity: ${fragmentClass.name} autoHideIme: $isAutoHideIme")
        }
        supportFragmentManager.beginTransaction().replace(v.id, instance).commit() //todo 增加tag。
        return v
    }
}