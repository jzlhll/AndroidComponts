package com.au.module_android.base

import android.content.Context
import android.content.Intent
import android.os.Build
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
import com.au.module_android.arct.BaseViewActivity
import com.au.module_android.permissions.IPermission
import com.au.module_android.permissions.OwnerForResult

/**
 * @author allan.jiang
 * Date: 2023/8/29
 * Description 限制
 */
class FragmentRootActivity : BaseViewActivity() {
    companion object {
        const val KEY_FRAGMENT_CLASS = "FragmentRootActivity_key_fragment"
        const val KEY_FRAGMENT_ARGUMENTS = "FragmentRootActivity_key_arguments"

        /**
         * 把一个Fragment放到本Activity当做唯一的界面。
         */
        fun start(context: Context,
                            fragmentClass:Class<out Fragment>,
                            arguments: Bundle? = null,
                            optionsCompat: ActivityOptionsCompat? = null)  {
            val intent = Intent(context, FragmentRootActivity::class.java)
            intent.putExtra(KEY_FRAGMENT_CLASS, fragmentClass)
            if (arguments != null) intent.putExtra(KEY_FRAGMENT_ARGUMENTS, arguments)

            ActivityCompat.startActivity(
                context,
                intent,
                optionsCompat?.toBundle()
            )
        }

        /**
         * 把一个Fragment放到本Activity当做唯一的界面，并在返回的时候带上结果。
         * <R> 代表你要返回的结果
         */
        fun <R> startForResult(context: Context,
                               fragmentClass:Class<out Fragment>,
                               ownerForResult: IPermission<Intent, R>,
                               onResultCallback: (R) -> Unit,
                               arguments: Bundle? = null,
                               optionsCompat: ActivityOptionsCompat? = null) {
            val intent = Intent(context, FragmentRootActivity::class.java)
            intent.putExtra(KEY_FRAGMENT_CLASS, fragmentClass)
            if (arguments != null) intent.putExtra(KEY_FRAGMENT_ARGUMENTS, arguments)

            ownerForResult.start(intent, optionsCompat) {
                result-> onResultCallback(result)
            }
        }
    }

    override fun onCommonCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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