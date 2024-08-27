package com.au.module_android.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.au.module.android.BuildConfig
import com.au.module_android.permissions.activity.ActivityForResult
import com.au.module_android.ui.views.ViewActivity
import com.au.module_android.utils.startActivityFix
import com.au.module_android.utils.unsafeLazy

/**
 * @author au
 * Date: 2023/8/29
 * Description 限制
 */
open class FragmentRootActivity : ViewActivity() {
    companion object {
        const val KEY_FRAGMENT_CLASS = "FragmentRootActivity_key_fragment"
        const val KEY_FRAGMENT_ARGUMENTS = "FragmentRootActivity_key_arguments"
        const val KEY_HAS_WEB_VIEW = "FragmentRootActivity_key_has_web_view"

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
                            autoHideIme: Boolean = false,
                            activityResultCallback:ActivityResultCallback<ActivityResult>? = null) {
            start(context, FragmentRootActivity::class.java, fragmentClass, activityResult, arguments, optionsCompat, hasWebView, autoHideIme, activityResultCallback)
        }

        internal fun start(context: Context,
                           showActivityClass:Class<out Activity>,
                           fragmentClass:Class<out Fragment>,
                           activityResult:ActivityForResult?,
                           arguments: Bundle?,
                           optionsCompat: ActivityOptionsCompat?,
                           hasWebView:Boolean,
                           autoHideIme:Boolean,
                           activityResultCallback:ActivityResultCallback<ActivityResult>? = null) {
            val intent = Intent(context, showActivityClass)
            intent.putExtra(KEY_FRAGMENT_CLASS, fragmentClass)
            intent.putExtra(KEY_INTENT_AUTO_HIDE_IME, autoHideIme)
            if(hasWebView) intent.putExtra(KEY_HAS_WEB_VIEW, hasWebView)
            if (arguments != null) intent.putExtra(KEY_FRAGMENT_ARGUMENTS, arguments)

            if (activityResult != null) {
                activityResult.start(intent, optionsCompat, activityResultCallback)
            } else {
                context.startActivityFix(intent, optionsCompat?.toBundle())
            }
        }
    }

    val fragmentClass by unsafeLazy { intent.getSerializableExtra(KEY_FRAGMENT_CLASS) as Class<Fragment> }

    override fun onUiCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = FragmentContainerView(inflater.context)
        v.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        v.id = View.generateViewId()
        val instance = fragmentClass.getDeclaredConstructor().newInstance()
        instance.arguments = intent.getBundleExtra(KEY_FRAGMENT_ARGUMENTS)
        if (BuildConfig.DEBUG) {
            Log.d("AU_APP", "FragmentRootActivity: ${fragmentClass.name} autoHideIme: $isAutoHideIme")
        }
        supportFragmentManager.beginTransaction().replace(v.id, instance).commit() //todo 增加tag。
        return v
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidBug5497Workaround.assistActivity(this)
    }
}