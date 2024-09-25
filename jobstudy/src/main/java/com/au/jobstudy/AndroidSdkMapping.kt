package com.au.jobstudy

import android.os.Build

/**
 * @author au
 * @date :2024/2/2 14:44
 * @description:
 */
class AndroidSdkMapping {
    val map = hashMapOf(35 to "Android 15",
                        34 to "Android 14(UpsideDownCake)",
                        33 to "Android 13 (Android Tiramisu)",
                        32 to "Android 12L (Android Sv2)",
                        31 to "Android 12 (Android S)",
                        30 to "Android 11 (Android R)",
                        29 to "Android 10 (Android Q)",
                        28 to "Android 9 Pie",
                        27 to "Android 8.1 Oreo(Android O)",
                        26 to "Android 8.0 Oreo(Android O)",
                        25 to "Android 7.1 N",
                        24 to "Android 7.0 N",
                        23 to "Android 6.0 M",
        )

    val currentVersionStr = map[Build.VERSION.SDK_INT]
}