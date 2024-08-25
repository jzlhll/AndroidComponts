package com.allan.autoclickfloat.activities.recordprojects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allan.autoclickfloat.consts.Const
import com.allan.autoclickfloat.database.AppDatabase
import com.allan.autoclickfloat.database.Project
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logt

class RecordProjectsAllViewModel : ViewModel() {
    val allProjectsData = NoStickLiveData<List<Project>>()

    fun getAll() {
        viewModelScope.launchOnThread {
            logt { Const.TAG + " RecordProjectsAllFragment load data..." }
            val all = AppDatabase.db.projectDao().getAll()
            allProjectsData.setValueSafe(all)
        }
    }
}