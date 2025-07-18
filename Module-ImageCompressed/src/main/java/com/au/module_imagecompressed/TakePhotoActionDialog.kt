package com.au.module_imagecompressed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Space
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.au.module_android.click.onClick
import com.au.module_android.ui.views.ViewFragment
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.dp
import com.au.module_androidui.R
import com.au.module_androidui.databinding.SimpleTextBinding
import com.au.module_androidui.dialogs.FragmentBottomSheetDialog

/**
 * @author allan.jiang
 * Date: 2023/4/25
 * Description 底部弹窗用于操作。实现接口ITakePhotoActionDialogCallback。
 */
class TakePhotoActionDialog : ViewFragment() {
    interface ITakePhotoActionDialogCallback {
        fun onClickTakePic()
        fun onClickSelectPhoto()

        /**
         * 什么也没有点击，关闭了。
         */
        fun onNothingClosed()
    }

    companion object {
        /**
         * 通过在parentFragment(RecipeDetailFragment)之上显示的dialog。因此是在一个activity中。
         * pop出来的。
         */
        fun pop(owner: LifecycleOwner, cameraText:String="Camera", photosText:String="Select from Photos") {
            if (owner !is ITakePhotoActionDialogCallback) {
                throw IllegalArgumentException("pop owner must implement ITakePhotoActionDialogCallback")
            }
            val isFragment = owner is Fragment
            val b = bundleOf(
                "isFragment" to isFragment,
                "cameraText" to cameraText,
                "photosText" to photosText
            )
            if (isFragment) {
                FragmentBottomSheetDialog.Companion.show<TakePhotoActionDialog>(owner.childFragmentManager, b)
                return
            }
            if (owner is AppCompatActivity) {
                FragmentBottomSheetDialog.Companion.show<TakePhotoActionDialog>(owner.supportFragmentManager, b)
                return
            }
            throw IllegalArgumentException("pop owner must be Fragment or AppCompatActivity")
        }
    }

    private val isFragment by lazy {
        arguments?.getBoolean("isFragment") ?: false
    }

    private val clickCallback:ITakePhotoActionDialogCallback?
        get() = if(isFragment) parentFragment?.parentFragment.asOrNull()
                else parentFragment?.activity?.asOrNull()

    private fun createBottomSpace() : View {
        val space = Space(requireActivity())
        space.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 24.dp)
        return space
    }

    private fun dismiss() {
        parentFragment.asOrNull<DialogFragment>()?.dismissAllowingStateLoss()
    }

    private var mIsClicked = false
    private val cameraText by lazy {
        arguments?.getString("cameraText") ?: "Camera"
    }
    private val photosText by lazy {
        arguments?.getString("photosText") ?: "Select from Photos"
    }

    private fun createTakePhoto(): View {
        return SimpleTextBinding.inflate(requireActivity().layoutInflater, null, false).also {
            it.root.text = cameraText
            it.root.onClick {
                mIsClicked = true
                clickCallback?.onClickTakePic()
            }
        }.root
    }

    private fun createSelectPhotos(): View {
        return SimpleTextBinding.inflate(requireActivity().layoutInflater, null, false).also {
            it.root.text = photosText
            it.root.onClick {
                mIsClicked = true
                clickCallback?.onClickSelectPhoto()
            }
        }.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if(!mIsClicked) clickCallback?.onNothingClosed()
    }

    override fun onUiCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val h = bottomActionItemHeight(this).toInt()
        return LinearLayout(inflater.context).also {
            root = it
            it.orientation = LinearLayout.VERTICAL
            it.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

            val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, h)
            it.addView(createTakePhoto(), layoutParams)
            it.addView(createSelectPhotos(), layoutParams)
            it.addView(createBottomSpace())
            //监听界面被盖住立刻关闭本界面。避免宿主被系统回收后面报错
            parentFragment?.lifecycle?.addObserver(object: DefaultLifecycleObserver {
                override fun onPause(owner: LifecycleOwner) {
                    super.onPause(owner)
                    dismiss()
                }
            })
        }
    }

    fun bottomActionItemHeight(fragment: Fragment) : Float {
        return fragment.resources.getDimension(R.dimen.action_dialog_item_height)
    }
}