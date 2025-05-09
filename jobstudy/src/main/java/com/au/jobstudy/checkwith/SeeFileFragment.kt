package com.au.jobstudy.checkwith

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.VideoView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.au.module_android.glide.glideSetAny
import com.au.module_android.ui.views.ViewFragment
import com.au.module_android.utilsmedia.MediaHelper
import com.au.module_android.utils.dp
import com.au.module_android.utils.getScreenFullSize
import com.au.module_android.utils.unsafeLazy
import com.au.module_androidui.dialogs.FragmentBottomSheetDialog
import java.io.File

/**
 * @author allan
 * @date :2024/7/18 10:26
 * @description:
 */
class SeeFileFragment : ViewFragment() {
   companion object {
       fun showInDialog(host: Fragment, file:String) {
           val screenSize = host.requireActivity().getScreenFullSize()

           FragmentBottomSheetDialog.show<SeeFileFragment>(host.childFragmentManager, bundleOf("file" to file), height = screenSize.second)
       }
   }

    private val fileStr by unsafeLazy { arguments?.getString("file") }
    private val file : File by unsafeLazy { File(fileStr!!) }

    override fun onUiCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        return LinearLayout(inflater.context).also { it->
            it.orientation = LinearLayout.VERTICAL
            it.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also {
                it.leftMargin = 2.dp
                it.rightMargin = 2.dp
            }
            val mimeType = MediaHelper.getMimeTypePath(fileStr!!)
            if (mimeType.contains("video")) {
                it.addView(VideoView(inflater.context).also {
                    it.setVideoPath(fileStr)
                    it.start()
                })
            } else if (mimeType.contains("image")) {
                it.addView(AppCompatImageView(inflater.context).also {
                    it.glideSetAny(file)
                })
            } else if (mimeType.contains("audio")) {
                //todo
            }
        }
    }
}