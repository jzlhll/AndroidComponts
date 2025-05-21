package com.allan.androidlearning.transfer.views

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.allan.androidlearning.EntryActivity
import com.allan.androidlearning.transfer.CACHE_IMPORT_DIR
import com.allan.androidlearning.transfer.KEY_AUTO_ENTER_SEND_VIEW
import com.allan.androidlearning.transfer.KEY_START_TYPE
import com.allan.androidlearning.transfer.MY_DROID_SHARE_IMPORT_URIS
import com.allan.androidlearning.transfer.MyDroidConst
import com.allan.androidlearning.transfer.MyDroidKeepLiveService
import com.allan.androidlearning.transfer.benas.UriRealInfoEx
import com.au.module_android.Globals
import com.au.module_android.simplelivedata.asNoStickLiveData
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.ui.views.ViewActivity
import com.au.module_android.utils.findCustomFragmentGetActivity
import com.au.module_android.utils.findEntryActivity
import com.au.module_android.utils.findLaunchActivity
import com.au.module_android.utils.launchOnIOThread
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.logt
import com.au.module_android.utils.parcelableArrayListExtraCompat
import com.au.module_android.utils.parcelableExtraCompat
import com.au.module_android.utils.startActivityFix
import com.au.module_android.utilsmedia.copyToCacheConvert
import com.au.module_android.utilsmedia.getRealInfo
import com.au.module_android.utilsmedia.isFromMyApp

class ShareImportActivity : ViewActivity() {
    override fun onDestroy() {
        super.onDestroy()

        MyDroidKeepLiveService.Companion.stopMyDroidAlive()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dealWithIntent(intent)
    }

    private fun dealWithIntent(intent: Intent?) {
        val sharedImportUris = mutableListOf<Uri>()
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                // 处理单文件分享
                val uri: Uri? = intent.parcelableExtraCompat(Intent.EXTRA_STREAM)
                uri?.let { sharedImportUris.add(it) }
            }

            Intent.ACTION_SEND_MULTIPLE -> {
                // 处理多文件分享
                intent.parcelableArrayListExtraCompat<Uri>(Intent.EXTRA_STREAM)?.let { uris->
                    sharedImportUris.addAll(uris)
                }
            }
        }
        handleIncreaseUris(sharedImportUris)
        intent?.removeExtra(Intent.EXTRA_STREAM)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        dealWithIntent(intent)
    }

    private fun ifUrisFromMyApp(sharedImportUris: List<Uri>) : Boolean{
        var isFromMyApp = false
        for (uri in sharedImportUris) {
            if (uri.isFromMyApp(this@ShareImportActivity)) {
                isFromMyApp = true
                break
            }
        }
        return isFromMyApp
    }

    private fun parseImportList(uris: List<Uri>, newImportList:MutableList<UriRealInfoEx>) : Boolean{
        val map = MyDroidConst.sendUriMap.realValue ?: hashMapOf()
        val oldList = map.values.toList()

        var hasNoPath = false
        uris.forEach { uri->
            if (oldList.find { it.uri == uri } == null) {
                val real = uri.getRealInfo(Globals.app)
                if (real.goodPath() == null) {
                    hasNoPath = true
                }
                val bean = UriRealInfoEx.copyFrom(real)
                newImportList.add(bean)
            }
        }

        return hasNoPath
    }

    private fun handleIncreaseUris(uris: List<Uri>) {
        logdNoFile { "handle increase uris $uris" }

        if (ifUrisFromMyApp(uris)) {
            Toast.makeText(this, "请点击\"导入到发送列表\"，而不是\"分享\"！", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val newImportList = mutableListOf<UriRealInfoEx>()
        val hasNoPath = parseImportList(uris, newImportList)

        if (!hasNoPath) { //不需要等待，直接信息转换，存入并结束本activity
            importMapAndJumpFinish(newImportList)
        } else { //需要转圈等待拷贝，转成本地cache，uri。再finish
            lifecycleScope.launchOnIOThread {
                val newImportCacheList = mutableListOf<UriRealInfoEx>()
                for (bean in newImportList) {
                    val copiedFileUri = bean.uri.copyToCacheConvert(contentResolver, null, CACHE_IMPORT_DIR)
                    val info = copiedFileUri.getRealInfo(this@ShareImportActivity)
                    newImportCacheList.add(UriRealInfoEx.copyFrom(info))
                }
                logt { "newImportCacheList $newImportCacheList" }
                importMapAndJumpFinish(newImportCacheList)
            }
        }
    }

    private fun importMapAndJumpFinish(newImportList: MutableList<UriRealInfoEx>) {
        val map = MyDroidConst.sendUriMap.realValue ?: hashMapOf()
        map.putAll(newImportList.map { it.uriUuid to it })
        MyDroidConst.sendUriMap.asNoStickLiveData().setValueSafe(map)
        jumpNext()
        finish()
    }

    private fun jumpNext() {
        val found = findEntryActivity(EntryActivity::class.java)
        //清理掉自己
        val foundShellActivity = findCustomFragmentGetActivity(ShareReceiverFragment::class.java)
        foundShellActivity?.finish()

        if (!found) { //说明app没有启动过。需要先启动下首页，借过一下。
            val intent = findLaunchActivity(Globals.app).first
            intent.putExtra(KEY_START_TYPE, MY_DROID_SHARE_IMPORT_URIS)
            logdNoFile { "start entry activity " + intent.extras }
            startActivityFix(intent)
        } else { //app启动过了。有主界面，则直接跳入到ShareFragment
            FragmentShellActivity.Companion.start(
                this, ShareReceiverFragment::class.java,
                bundleOf(KEY_AUTO_ENTER_SEND_VIEW to true)
            )
        }
    }

    override fun onUiCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return RelativeLayout(inflater.context)
    }
}