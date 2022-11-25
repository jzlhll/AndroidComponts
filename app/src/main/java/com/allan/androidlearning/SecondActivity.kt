package com.allan.androidlearning

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.allan.androidlearning.databinding.ActivitySecondBinding
import com.allan.androidlearning.views.NestedCoordinatorLayout
import com.allan.androidlearning.views.RcvAndIndicatorMotion
import com.allan.androidlearning.views.RecyclerViewTester
import kotlinx.coroutines.*

class SecondActivity : AppCompatActivity() {

    private suspend fun loadingData() {
        Thread {
            Thread.sleep(2000)

        }.start()
        val r = withContext(Dispatchers.Default) {
            delay(2000)
            1
        }
    }

    private lateinit var motion:RcvAndIndicatorMotion
    lateinit var binding:ActivitySecondBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bind = ActivitySecondBinding.inflate(layoutInflater)
        binding = bind
        motion = RcvAndIndicatorMotion(bind.root, bind.recyclerView, bind.progressIcon)
        motion.loadingDataCallback = {
            lifecycleScope.launch {
                loadingData()
                motion.reset()
            }
        }
        setContentView(bind.root)
        RecyclerViewTester().test(this, bind.recyclerView, bind.root)
        //val list: BindListView = findViewById(R.id.listview)
        //RecyclerViewTester().test(this, list.recyclerView, list)
    }
}