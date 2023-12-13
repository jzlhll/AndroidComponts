package com.au.module_androidex

import androidx.lifecycle.LifecycleOwner
import com.au.module_android.permissions.createActivityForResult

/**
 * @author allan.jiang
 * @date :2023/12/13 10:48
 * @description:
 */
class SystemTakePictureHelper(owner:LifecycleOwner) {
    val contractHelper = createActivityForResult {  }
}