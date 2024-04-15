package com.allan.nongyaofloat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.accessibility.AccessibilityManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.allan.nongyaofloat.databinding.NongyaoActivityBinding
import com.au.module_android.click.onClick

class NongYaoActivity : AppCompatActivity() {
    private lateinit var mBinding:NongyaoActivityBinding
    private val tag = "NongyaoActivity"

    val permissionHelper = Permission

    private fun isAccessibilityEnabled() : Boolean {
        val accessibilityMgr = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        return accessibilityMgr.isEnabled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val vb = NongyaoActivityBinding.inflate(layoutInflater)
        mBinding = vb
        setContentView(vb.root)

        initNotification()
        initListener()
        startAutoClickService()


    }

    private fun initListener() {
        mBinding.requestPermissionsBtn.onClick {
            if (isAccessibilityEnabled()) {

            } else {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
        }

        btn_accessibility.setOnClickListener {
            checkAccessibility()
        }

        btn_floating_window.setOnClickListener {
            checkFloatingWindow()
        }

        btn_show_window.setOnClickListener {
            hideKeyboard()
            if (TextUtils.isEmpty(et_interval.text.toString())) {
                Snackbar.make(et_interval, "请输入间隔", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showFloatingWindow(et_interval.text.toString().toLong())
        }

        btn_close_window.setOnClickListener {
            closeFloatWindow()
        }

        btn_test.setOnClickListener {
            Log.d(TAG, "btn_test on click")
        }
    }
}