package com.allan.autoclickfloat.activities.autofs

import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.au.module_android.Globals
import com.au.module_cached.AppDataStore
import java.util.UUID

/**
 * @author allan
 * @date :2024/9/25 15:36
 * @description:
 */
class AutoFsWorkHelper {
    private val workTag = "autoFS"

    private val workManager:WorkManager
        get() {
            return WorkManager.getInstance(Globals.app)
        }

    private var _isOneTime:Boolean? = null
    private var isOneTime:Boolean
        get() {
            val oneTime = _isOneTime ?:
                AppDataStore.readBlocked("auto_fs_work_is_one_time", false).also { _isOneTime = it }
            return oneTime
        }

        set(value) {
            _isOneTime = value
            AppDataStore.save("auto_fs_work_is_one_time" to value)
        }

    private var _oneTimeWorkerId:String? = null
    private var oneTimeWorkerId:String?
        get() {
            val id = _oneTimeWorkerId ?:
                AppDataStore.readBlocked("auto_fs_work_one_time_worker_id", "").also { _oneTimeWorkerId = it }
            return id
        }

        set(value) {
            if (value != null) {
                AppDataStore.save("auto_fs_work_one_time_worker_id" to value)
                _oneTimeWorkerId = value
            } else {
                AppDataStore.remove<String>("auto_fs_work_one_time_worker_id")
                _oneTimeWorkerId = ""
            }
        }

    fun allWorks() {

    }

    fun enqueueWork() {
        if (isOneTime) {
//            val constraints = Constraints.Builder()
//                .setRequiresCharging(false)
//                .setRequiresDeviceIdle(false)
//                .setRequiredNetworkType()
            val workRequest = OneTimeWorkRequest.Builder(AutoFsWorker::class.java)
                //.setConstraints(constraints)
                .addTag(workTag)
                .build()
            workManager.enqueue(workRequest)
        }
    }
}