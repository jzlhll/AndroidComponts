package com.au.module_android.arct

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment

/**
 * @author allan.jiang
 * Date: 2023/7/10
 * Description 基础Fragment的通用
 */
abstract class BaseViewFragment : Fragment(), ICommon {
    var root:View? = null

    /**
     * onCreateView
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return onCommonCreateView(layoutInflater, null, savedInstanceState).also { root = it }
    }

    /**
     *  如果想支持点击返回后，正常退出。则默认true。如何不想让它返回则返回true。
     */
    open val isNormalBackExit
        get() = true

    /**
     *  如果想支持back，比如一些别的逻辑，比如堆栈自身内部返回处理。
     */
    open val customBackAction:(()->Unit)? = null

    /**
     * onViewCreated
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                customBackAction?.invoke()
                if (isNormalBackExit) {
                    requireActivity().finishAfterTransition()
                }
            }
        })
        onCommonAfterCreateView(this, savedInstanceState, resources)
    }

    /**
     * 通常和viewPager搭配
     */
    open fun getTitle(): CharSequence? {
        return activity?.title
    }

    /**
     * 设置title
     */
    open fun setTitle(text: CharSequence) {
        activity?.title = text
    }

    /**
     * 设置title
     */
    open fun setTitle(text: Int) {
        activity?.setTitle(text)
    }
}