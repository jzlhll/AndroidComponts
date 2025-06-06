package com.allan.mydroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.os.bundleOf
import com.allan.mydroid.globals.KEY_START_TYPE
import com.allan.mydroid.ui.theme.AndroidCompontsTheme
import com.allan.mydroid.views.MyDroidAllFragment
import com.au.module_android.ui.FragmentShellActivity

class MyDroidActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        setContent {
//            AndroidCompontsTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
//            }
//        }

        FragmentShellActivity.start(this@MyDroidActivity, MyDroidAllFragment::class.java,
            bundleOf(KEY_START_TYPE to intent.getStringExtra(KEY_START_TYPE))
        )
        intent.removeExtra(KEY_START_TYPE)
        finish()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidCompontsTheme {
        Greeting("Android")
    }
}