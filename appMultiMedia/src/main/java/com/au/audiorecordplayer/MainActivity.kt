package com.au.audiorecordplayer

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.au.audiorecordplayer.bt.BleTestFragment
import com.au.audiorecordplayer.cam1.Camera1Fragment
import com.au.audiorecordplayer.cam2.Camera2Fragment
import com.au.audiorecordplayer.camx.CameraXFragment
import com.au.module_android.click.onClick
import com.au.module_android.ui.FragmentShellActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<View>(R.id.audioRecorder).onClick {
            FragmentShellActivity.start(this@MainActivity, AudioRecorderTestFragment::class.java)
        }
        findViewById<View>(R.id.audioPlayer).onClick {
            FragmentShellActivity.start(this@MainActivity, AudioPlayerTestFragment::class.java)
        }
        findViewById<View>(R.id.bluetooth).onClick {
            BleTestFragment.start(this@MainActivity, false)
        }
        findViewById<View>(R.id.bluetoothBle).onClick {
            BleTestFragment.start(this@MainActivity, true)
        }
        findViewById<View>(R.id.camera1).onClick {
            FragmentShellActivity.start(this@MainActivity, Camera1Fragment::class.java)
        }
        findViewById<View>(R.id.camera1TextureView).onClick {
            FragmentShellActivity.start(this@MainActivity, Camera1Fragment::class.java)
        }
        findViewById<View>(R.id.camera2).onClick {
            FragmentShellActivity.start(this@MainActivity, Camera2Fragment::class.java)
        }
        findViewById<View>(R.id.camerax).onClick {
            FragmentShellActivity.start(this@MainActivity, CameraXFragment::class.java)
        }
    }
}
