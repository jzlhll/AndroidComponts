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
import com.au.module_android.BuildConfig
import com.au.module_android.permissions.activity.ActivityForResult
import com.au.module_android.ui.base.AbsFragment
import com.au.module_android.ui.base.IFullWindow
import com.au.module_android.ui.views.ViewActivity
import com.au.module_android.utils.asOrNull
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

        /**
         * 把一个Fragment放到本Activity当做唯一的界面。
         *
         * @param context Context
         * @param fragmentClass 需要显示的fragment的类
         * @param arguments 用来透传给Fragment
         * @param optionsCompat 是startActivity的参数
         */
        fun start(context: Context,
                            fragmentClass:Class<out Fragment>,
                            arguments: Bundle? = null,
                            optionsCompat: ActivityOptionsCompat? = null,
                            activityResultCallback:ActivityResultCallback<ActivityResult>? = null) {
            startRoot(context, FragmentRootActivity::class.java, fragmentClass, null, arguments, optionsCompat, activityResultCallback)
        }

        /**
         * 把一个Fragment放到本Activity当做唯一的界面。
         *
         * @param context Context
         * @param fragmentClass 需要显示的fragment的类
         * @param activityResult 如果传入了非空对象，则会通过它启动，会携带返回；否则就是默认启动。
         * @param arguments 用来透传给Fragment
         * @param optionsCompat 是startActivity的参数
         */
        fun startForResult(context: Context,
                  fragmentClass:Class<out Fragment>,
                  activityResult:ActivityForResult,
                  arguments: Bundle? = null,
                  optionsCompat: ActivityOptionsCompat? = null,
                  activityResultCallback:ActivityResultCallback<ActivityResult>? = null) {
            startRoot(context, FragmentRootActivity::class.java, fragmentClass, activityResult, arguments, optionsCompat, activityResultCallback)
        }

        internal fun startRoot(context: Context,
                           showActivityClass:Class<out Activity>,
                           fragmentClass:Class<out Fragment>,
                           activityResult:ActivityForResult?,
                           arguments: Bundle?,
                           optionsCompat: ActivityOptionsCompat?,
                           activityResultCallback:ActivityResultCallback<ActivityResult>? = null) {
            val intent = Intent(context, showActivityClass)
            intent.putExtra(KEY_FRAGMENT_CLASS, fragmentClass)
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

        mIsAutoHideIme = instance.asOrNull<AbsFragment>()?.isAutoHideIme() ?: false

        if (BuildConfig.DEBUG) {
            Log.d("AU_APP", "FragmentRootActivity: ${fragmentClass.name}")
        }

        if (BuildConfig.ENABLE_EDGE_TO_EDGE) {
            //根据fragment情况来实现
            if (instance is IFullWindow) {
                //精髓所在：通过fragment的接口函数来判断是否updatePadding StatusBar或者NavBar。
                instance.fullPaddingEdgeToEdge(this, window, v)
            } else {
                super.setEdge(v)
            }
        }

        supportFragmentManager.beginTransaction().replace(v.id, instance).commit() //todo 增加tag。
        return v
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        AndroidBug5497Workaround.assistActivity(this)
//    }

    final override fun setEdge(contentView: View?) {
        //empty 因为我们其实要判断Fragment中的padding函数。
        //放在（instance is IFullWindow）去做。
    }

    private var mIsAutoHideIme = false

    final override fun isAutoHideIme(): Boolean {
        return mIsAutoHideIme
    }
}