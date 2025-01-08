package com.allan.autoclickfloat.activities.recordprojects

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.allan.autoclickfloat.databinding.RecordProjectOneFragmentBinding
import com.au.module_android.click.onClick
import com.au.module_android.ui.FragmentRootActivity
import com.au.module_android.ui.bindings.BindingParamsFragment
import com.au.module_android.utils.gone
import com.au.module_android.utils.logd
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.utils.visible

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

    private var adapter : RecordOneProjectAdapter? = null

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        val projectName = getTempParams<String>("projectName")
        val projectId = getTempParams<Int>("projectId")

        val isAddMode = getTempParams<Boolean>("isAddMode") ?: false
        if (isAddMode) {
           // binding.toolBar.title.text = "新建方案"
            binding.descCenter.visible()
            binding.descBottom.gone()
            binding.addStepCenterBtn.visible()
            binding.deleteStepBtn.gone()
        } else {
         //   binding.toolBar.title.text = projectName ?: ""
            binding.descCenter.gone()
            binding.descBottom.visible()
            binding.addStepCenterBtn.gone()
            binding.deleteStepBtn.visible()
        }

        binding.deleteStepBtn.onClick {
            viewModel.switchDeleteMode()
        }

        viewModel.stepsData.observe(this) {
            refreshRcv(it)
        }

        binding.addStepCenterBtn.onClick {
            addStep()
        }
        if (projectId != null && projectId >= 0) {
            viewModel.loadProjectIdSteps(projectId)
        }
    }

    private fun addStep() {
        FragmentRootActivity.start(requireContext(), AllAppListFragment::class.java)
    }


    private fun refreshRcv(steps:List<StepWrap>) {
        if (adapter == null) {
            binding.rcv.adapter = RecordOneProjectAdapter().also { adapter = it }
            binding.rcv.layoutManager = LinearLayoutManager(requireContext()).also { it.orientation = LinearLayoutManager.VERTICAL }
            binding.rcv.setHasFixedSize(true)
        }

        adapter?.initDatas(steps, false)
    }
}