package com.allan.androidlearning

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.allan.androidlearning.views.RecyclerViewTester

class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        RecyclerViewTester().test(this, findViewById(R.id.recyclerview), findViewById(R.id.main_content))
    }
}