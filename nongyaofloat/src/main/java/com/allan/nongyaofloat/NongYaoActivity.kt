package com.allan.nongyaofloat

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.au.module_android.permissions.requestFloatWindowPermission

class NongYaoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//
//        requestFloatWindowPermission(this) {hasPermission, isDirect->
//            if (hasPermission) {
//                showFloat()
//            } else {
//                if (isDirect) {
//                    Toast.makeText(applicationContext, "请授权悬浮窗权限！", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(applicationContext, "您没有授权权限，不能展示！", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun showFloat() {
        TODO("Not yet implemented")
    }
}