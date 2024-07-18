package com.au.jobstudy.checkwith.base

import androidx.viewbinding.ViewBinding
import com.au.jobstudy.checkwith.CheckWithFragment
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.asOrNull
import java.io.File

/**
 * @author allan
 * @date :2024/7/16 11:16
 * @description:
 */
abstract class FirstResumeBindingFragment<VB: ViewBinding> : BindingFragment<VB>() {
    var firstStartCallback:(()->Unit)? = null

    abstract fun getUploadFiles():List<String>

    override fun onStart() {
        super.onStart()
        firstStartCallback?.invoke()
        firstStartCallback = null
    }

    val checkWithFragment:CheckWithFragment?
        get() = parentFragment.asOrNull<CheckWithFragment>()

    abstract fun usedFiles():List<File>

    //最后清理一下
    val tempFiles = mutableListOf<File>()
    fun clearNoUsedFile() {
        val existFiles = usedFiles()
        for (file in tempFiles) {
            if (!existFiles.contains(file)) {
                try {
                    if(file.exists()) file.delete()
                } catch (e:Exception) {}
            }
        }
    }
}