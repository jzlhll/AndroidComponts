package com.allan.androidlearning

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.allan.androidlearning.views.BindListView
import com.allan.androidlearning.views.RecyclerViewTester

class ThirdActivity : AppCompatActivity() {
    private val pullDownTriggerValue = 160f
    private val TAG = "alland"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)
        val list: BindListView = findViewById(R.id.listview)
        RecyclerViewTester().test(this, list.recyclerView, list)
    }
}