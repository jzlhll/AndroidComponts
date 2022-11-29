package com.allan.androidlearning.sql

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.allan.androidlearning.R
import com.allan.androidlearning.databinding.ActivitySqlBinding
import com.allan.androidlearning.sql.sql.MySQLiteHelper

class SqlActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySqlBinding
    private val sqHelper by lazy(LazyThreadSafetyMode.NONE){
        MySQLiteHelper(applicationContext)
    }

    private val onClick : (View)->Unit = {
        when (it.id) {
            R.id.sqlBtn1 -> {
                val db = sqHelper.writableDatabase
                val cur = db.query(MySQLiteHelper.TABLE_NAME, null, null, null, null, null, null)
                cur.moveToFirst()
                while (true) {
                    Log.d(Const.TAG, "" + cur.getString(0) + cur.getString(cur.getColumnIndex("title")))
                    if (!cur.moveToNext()) {
                        break
                    }
                }

                cur.close()

                db.close()
            }
            R.id.sqlBtn2 -> {

            }
            R.id.sqlBtn3 -> {

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySqlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sqlBtn1.setOnClickListener(onClick)
        binding.sqlBtn2.setOnClickListener(onClick)
        binding.sqlBtn3.setOnClickListener(onClick)
    }
}