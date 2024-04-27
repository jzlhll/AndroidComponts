package com.allan.autoclickfloat.activities.recordprojects

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.allan.autoclickfloat.consts.Const
import com.allan.autoclickfloat.database.Step
import com.allan.autoclickfloat.databinding.RecordProjectOneFragmentBinding
import com.au.module_android.ui.FragmentRootActivity
import com.au.module_android.ui.bindings.BindingParamsFragment
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.unsafeLazy

class RecordOneProjectFragment : BindingParamsFragment<RecordProjectOneFragmentBinding>() {
    companion object {
        fun startAsAdd(context: Context) {
            putTempParams(RecordOneProjectFragment::class.java,
                "isAddMode" to true)
            FragmentRootActivity.start(context, RecordOneProjectFragment::class.java)
        }

        fun startAsEdit(context:Context, projectName:String, projectId:Int) {
            putTempParams(RecordOneProjectFragment::class.java,
                "isAddMode" to false,
                        "projectName" to projectName,
                        "projectId" to projectId)
            FragmentRootActivity.start(context, RecordOneProjectFragment::class.java)
        }
    }

    private val viewModel by unsafeLazy { ViewModelProvider(requireActivity())[RecordOneProjectViewModel::class.java] }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        val projectName = getTempParams<String>("projectName")
        val projectId = getTempParams<Int>("projectId")

        val isAddMode = getTempParams<Boolean>("isAddMode") ?: false
        if (isAddMode) {
            binding.toolBar.title.text = "新建方案"
        } else {
            binding.toolBar.title.text = projectName ?: ""
        }

        viewModel.stepsData.observe(this) {

        }

        if (projectId != null && projectId >= 0) {
            viewModel.loadProjectIdSteps(projectId)
        }

    }

    private fun refreshRcv(steps:List<Step>) {
        binding.rcv.adapter =
    }
}