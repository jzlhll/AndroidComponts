package com.au.module_android.ui

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
abstract class AbsFragment : Fragment(), IUi {
    /**
     * onCreateView
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return creatingView(layoutInflater, null, savedInstanceState)
    }

    /**
     *  如果想支持back，比如一些别的逻辑，比如堆栈自身内部返回处理。
     *  return true则表示期待返回直接退出。或者默认customBackAction = null，也是直接返回。
     */
    open val customBackAction:(()->Boolean)? = null

    /**
     * onViewCreated
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val action = customBackAction
                if (action == null) {
                    requireActivity().finishAfterTransition()
                } else {
                    if (action.invoke()) {
                        requireActivity().finishAfterTransition()
                    }
                }
            }
        })
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