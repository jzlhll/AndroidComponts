package com.allan.autoclickfloat.activities.recordprojects

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allan.autoclickfloat.database.AppDatabase
import com.allan.autoclickfloat.database.Step
import com.au.module_android.simplelivedata.SafeLiveData
import com.au.module_android.simplelivedata.asSafeLiveData
import com.au.module_android.utils.launchOnThread

class RecordOneProjectViewModel : ViewModel() {
    val stepsData:LiveData<List<Step>> = SafeLiveData()

    fun loadProjectIdSteps(projectId:Int) {
        viewModelScope.launchOnThread {
            val steps = AppDatabase.db.stepDao().getAll(projectId)
            stepsData.asSafeLiveData().setValueSafe(steps)
        }
    }
}