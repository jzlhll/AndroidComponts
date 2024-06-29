package com.allan.androidlearning

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.au.module_android.json.fromJson
import com.au.module_android.json.fromJsonList
import com.au.module_android.json.toJsonString
import com.au.module_android.utils.logd

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class JsonTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.allan.androidlearning", appContext.packageName)

    }

    @Test
    fun testJson() {
        val bean1 = Bean("first", 1)
        val bean2 = Bean("second", 2)

        val bean1Str = bean1.toJsonString()
        val bean2Str = bean2.toJsonString()

        val revert1 = bean1Str.fromJson<Bean>()
        val revert2 = bean2Str.fromJson<Bean>()

        val list = listOf(bean1, bean2)
        val listStr = list.toJsonString()
        val revertList = listStr.fromJsonList<Bean>()
        val revertList2 = listStr.fromJsonList(Bean::class.java)

        logd{"beans1 $bean1Str bean2str $bean2Str $listStr $revertList"}

        val str = ""
        val bytes = str.toByte()


    }
}