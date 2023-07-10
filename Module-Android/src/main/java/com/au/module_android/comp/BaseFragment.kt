package com.au.module_android.comp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment

/**
 * @author allan.jiang
 * Date: 2023/7/10
 * Description TODO
 */
abstract class BaseFragment : Fragment(), ICommon {
    /**
     * onCreateView
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return onCommonCreateView(inflater, container, savedInstanceState)
    }

    private var mOnBackAction:(()->Unit)? = null

    open fun initOnBackAction():(()->Unit)? = null

    /**
     * onViewCreated
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mOnBackAction = initOnBackAction()
        if (mOnBackAction == null) {
            mOnBackAction = {
                requireActivity().finishAfterTransition()
            }
        }

        if (mOnBackAction != null) {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    mOnBackAction?.invoke()
                }
            })
        }
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