package com.allan.autoclickfloat.activities.recordprojects

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allan.autoclickfloat.database.AppDatabase
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.simplelivedata.asNoStickLiveData
import com.au.module_android.utils.launchOnThread

class RecordOneProjectViewModel : ViewModel() {
    val stepsData:LiveData<List<StepWrap>> = NoStickLiveData()

    private var isDeleteMode = false

    fun loadProjectIdSteps(projectId:Int) {
        viewModelScope.launchOnThread {
            val steps = AppDatabase.db.stepDao().getAll(projectId).map {
                StepWrap(false, it)
            }
            stepsData.asNoStickLiveData().setValueSafe(steps)
        }
    }

    fun switchDeleteMode() {
        isDeleteMode = !isDeleteMode
        val list = stepsData.value ?: listOf()
        stepsData.asNoStickLiveData().setValueSafe(list.map { StepWrap(isDeleteMode, it.step) })
    }
}