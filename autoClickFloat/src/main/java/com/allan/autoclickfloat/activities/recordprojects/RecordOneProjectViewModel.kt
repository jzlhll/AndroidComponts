package com.allan.autoclickfloat.activities.recordprojects

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allan.autoclickfloat.database.AppDatabase
import com.au.module_android.simplelivedata.SafeLiveData
import com.au.module_android.simplelivedata.asSafeLiveData
import com.au.module_android.utils.launchOnThread

class RecordOneProjectViewModel : ViewModel() {
    val stepsData:LiveData<List<StepWrap>> = SafeLiveData()

    private var isDeleteMode = false

    fun loadProjectIdSteps(projectId:Int) {
        viewModelScope.launchOnThread {
            val steps = AppDatabase.db.stepDao().getAll(projectId).map {
                StepWrap(false, it)
            }
            stepsData.asSafeLiveData().setValueSafe(steps)
        }
    }

    fun switchDeleteMode() {
        isDeleteMode = !isDeleteMode
        val list = stepsData.value ?: listOf()
        stepsData.asSafeLiveData().setValueSafe(list.map { StepWrap(isDeleteMode, it.step) })
    }
}