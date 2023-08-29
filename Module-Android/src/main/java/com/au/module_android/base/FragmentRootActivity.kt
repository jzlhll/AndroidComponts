package com.au.module_android.base

import android.content.Context
import android.content.Intent
import android.content.res.Resources
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
        fun startFragmentActivity(context: Context,
                                  fragmentClass:Class<out Fragment>,
                                   arguments: Bundle? = null,
                                   optionsCompat: ActivityOptionsCompat? = null,
                                   intentBlock: (Intent) -> Unit = {
                                       ActivityCompat.startActivity(
                                           context,
                                           it,
                                           optionsCompat?.toBundle()
                                       )
                                   })  {
            val intent = Intent(context, FragmentRootActivity::class.java)
            intent.putExtra(KEY_FRAGMENT_CLASS, fragmentClass)
            arguments?.let {
                intent.putExtra(KEY_FRAGMENT_ARGUMENTS, arguments)
            }
            intentBlock.invoke(intent)
        }
    }

    private fun parseFragmentClass() : Class<Fragment> { //不做保护，出问题直接报错。
        return intent.getSerializableExtra(KEY_FRAGMENT_CLASS) as Class<Fragment>
    }

    override fun onCommonCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = FragmentContainerView(inflater.context)
        v.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        v.id = View.generateViewId()

        val fragmentClass = parseFragmentClass()
        val instance = fragmentClass.getDeclaredConstructor().newInstance()
        instance.arguments = intent.getBundleExtra(KEY_FRAGMENT_ARGUMENTS)
        if (BuildConfig.DEBUG) {
            Log.d(APP_TAG, "FragmentContainerActivity: ${fragmentClass.name} autoHideIme: $isAutoHideIme")
        }
        supportFragmentManager.beginTransaction().replace(v.id, instance).commit() //todo 增加tag。
        return v
    }
}