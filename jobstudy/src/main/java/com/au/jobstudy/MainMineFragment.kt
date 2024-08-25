package com.au.jobstudy

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.au.jobstudy.check.NameList
import com.au.jobstudy.databinding.FragmentMainMineBinding
import com.au.module_android.Globals
import com.au.module_android.screenadapter.ToutiaoScreenAdapter
import com.au.module_android.ui.bindings.BindingFragment
import kotlinx.coroutines.launch


class MainMineFragment : BindingFragment<FragmentMainMineBinding>() {
    private var clickDebugCount = 0

    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    fun getNavigationBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.name.text = NameList.NAMES_JIANG_TJ
        val appName = getString(R.string.app_name)
        val system = AndroidSdkMapping().currentVersionStr
        val name = "$appName${BuildConfig.VERSION_NAME} - versionCode:${BuildConfig.VERSION_CODE}\n$system"
        binding.logoText.text = name

        ViewCompat.setOnApplyWindowInsetsListener(requireActivity().window.decorView) { _, insets ->
//            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
//            val bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
//
//            val top1 = getStatusBarHeight(requireContext())
//            val bottom1 = getNavigationBarHeight(requireContext())

//            if (BuildConfig.DEBUG) {
//                lifecycleScope.launch {
//                    val appRes = Globals.app.resources.displayMetrics
//                    val activityRes = requireActivity().resources.displayMetrics
//                    val adapter = ToutiaoScreenAdapter.toString()
//                    val sb = buildString {
//                        append(name)
//                        append("\nstatusBar $top ($top1) navBar $bottom ($bottom1)")
//                        append("\nappDensity ${appRes.density} activity ${activityRes.density}")
//                        append("\nappDensityDpi ${appRes.densityDpi} activity ${activityRes.densityDpi}")
//                        append("\n$adapter")
//                    }
//                    binding.logoText.text = sb
//                }
//            }

            insets
        }
    }
}