package com.au.module_android.utilsmedia

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.au.module_android.utils.startActivityFix
import com.au.module_android.utilsmedia.MediaHelper.Companion.getMimeTypePath
import java.io.File

/**
 * 将本地文件分享出去。
 * 一定是要在有分享权限的路径下。如果没有的话，参考xml/file_path.xml的写法。
 *
 * @param file 本应用范围内的文件
 */
fun shareFile(context: Context, file: File?) {
    if (file != null && file.exists()) {
        val share = Intent(Intent.ACTION_SEND)
        val uri: Uri?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // "项目包名.fileprovider"即是在清单文件中配置的authorities
            uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
            // 给目标应用一个临时授权
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            uri = Uri.fromFile(file)
        }

        share.putExtra(Intent.EXTRA_STREAM, uri)
        share.type = getMimeTypePath(file.absolutePath) // 此处可发送多种文件
        share.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivityFix(Intent.createChooser(share, "分享文件"))
    }
}