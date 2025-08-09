package com.au.audiorecordplayer.bt

import android.Manifest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.au.audiorecordplayer.util.MainUIManager
import com.au.module_android.permissions.createMultiPermissionForResult
import com.au.module_android.permissions.hasPermission
import com.au.module_cached.AppDataStore

/**
 * 必须在成员变量初始化。否则multiPermissionHelp不生效
 * Fragment {
 *   private val blePermissionHelp = BlePermissionHelp(this)
 * }
 */
class BtPermissionHelp(private val f: Fragment) {
    //android12上下高低版本的不同权限类型
    val blePermissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT)
        else
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION)

    val multiPermissionHelp = f.createMultiPermissionForResult(blePermissions)

    /**
     * 仅限于请求蓝牙，做一下这种控制；
     * android因为shouldShowRequestPermissionRationale不能，在第一次的时候不准确；所以默认返回true即可。
     *
     * warning: 调用之前，需要isPermissionGrant() = false，才调用这个函数。
     */
    fun canShowRequestDialogUi(activity: AppCompatActivity) : Boolean{
        //第一次使用默认true
        if (AppDataStore.readBlocked("permission_ble_showui_once", true)) {
            AppDataStore.save("permission_ble_showui_once", false)
            return true
        }
        //其他情况使用shouldShowRequestPermissionRationale，只要其一就可以显示ui
        for (permission in blePermissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true
            }
        }

        return false
    }

    fun safeRun(
        block: () -> Unit
    ) {
        multiPermissionHelp.safeRun(block, notGivePermissionBlock = {
            MainUIManager.get().toastSnackbar(f.view, "需要蓝牙相关的权限!")
        })
    }

    fun isPermissionGrant() : Boolean {
        return hasPermission(*blePermissions)
    }
}