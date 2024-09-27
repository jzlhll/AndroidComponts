package com.allan.autoclickfloat.activities.autofs

import android.app.Application
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.au.module_android.utils.logt
import com.au.module_android.utils.startOutActivity

/**
 * @author allan
 * @date :2024/9/25 15:36
 * @description:
 */
class AutoFsWorker(private val context: Application, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private fun doIt() {
        val pm = context.packageManager
        logt { "alland auto fs worker doIt!" }
        val intent = pm.getLaunchIntentForPackage("com.ss.android.lark")
        Toast.makeText(context, "时间到了，执行开始...", Toast.LENGTH_LONG).show()
        if (intent != null) {
            context.startOutActivity(intent)
        }
    }

    override fun doWork(): Result {
        doIt()
        return Result.success()
    }
}