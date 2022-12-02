package com.allan.androidlearning

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import com.allan.androidlearning.activities.LiveDataActivity
import com.allan.androidlearning.databinding.ActivityEntroBinding
import com.google.android.material.button.MaterialButton
import java.util.*

private val TAG = "EntroActivity"
class EntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEntroBinding

    private val activities:List<Class<out AppCompatActivity>> by lazy { getAllActivities() }

    private fun getAllActivities():List<Class<out AppCompatActivity>> {
        val packageManager = packageManager
        val packageInfo = packageManager.getPackageInfo(
            packageName, PackageManager.GET_ACTIVITIES
        )
        val list = (mutableListOf<Class<out AppCompatActivity>>())
        for (activity in packageInfo.activities) {
            val aClass = Class.forName(activity.name)
            if (AppCompatActivity::class.java.isAssignableFrom(aClass)) {
                list.add(aClass as Class<out AppCompatActivity>)
            }
        }
        return list
    }

    private val clickOnJumpBtn:((View?) ->Unit) = {
        val acs = activities
        if (it is MaterialButton) {
            val name = it.text.toString().lowercase(Locale.getDefault())
            val addedName = (name + "Activity").lowercase(Locale.getDefault())
            acs.forEach { clazz->
                val simpleName = clazz.simpleName.lowercase(Locale.getDefault())
                if (simpleName == name || simpleName == addedName) {
                    startActivity(Intent(applicationContext, clazz))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonsHost.forEach {
            if (it is MaterialButton) {
                (it).setOnClickListener(clickOnJumpBtn)
            }
        }

        setSupportActionBar(findViewById(R.id.toolbar))
    }
}
