package com.au.module_imagecompressed

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.WorkerThread
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.au.module_android.BuildConfig
import com.au.module_android.permissions.IContractResult
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.launchOnUi
import com.au.module_imagecompressed.util.MimeUtil
import com.au.module_imagecompressed.util.URI_COPY_PARAM_ANY_TO_JPG
import com.au.module_imagecompressed.util.URI_COPY_PARAM_HEIC_TO_JPG
import com.au.module_imagecompressed.util.UriUtil
import com.au.module_imagecompressed.util.copyToCacheConvert
import com.au.module_imagecompressed.util.getUriMimeType
import com.au.module_imagecompressed.util.length
import java.io.File

/**
 * @author allan
 * @date :2024/10/23 16:39
 * @description:
 */
class MultiPhotoPickerContractResult(
    private val fragment: Fragment,
    var max:Int,
    resultContract: ActivityResultContract<PickVisualMediaRequest, List<@JvmSuppressWildcards Uri>>)
    : IContractResult<PickVisualMediaRequest, List<@JvmSuppressWildcards Uri>>(fragment, resultContract) {
    enum class CopyMode {
        /**
         * 不做拷贝, 都是原始Uri。
         */
        COPY_NOTHING,

        //基本上所有都不做拷贝，原始Uri。只有heic图片做转换为jpg拷贝。
        COPY_NOTHING_BUT_CVT_HEIC,

        /** 基本上所有都不做拷贝，原始Uri。heic和png等图片能转为jpg都做拷贝。*/
        COPY_CVT_IMAGE,
    }

    enum class PickerType {
        IMAGE,
        VIDEO,
        IMAGE_AND_VIDEO,
    }

    private var mCopyMode: CopyMode = CopyMode.COPY_CVT_IMAGE

    private lateinit var oneByOneCallback:((UriWrap)->Unit)
    private var mLimitImageSize = 50 * 1024 * 1024
    private var mTargetImageSize = 5 * 1024 * 1024
    private var mLimitVideoSize = 100 * 1024 * 1024L
    private var ignoreSizeKb = 100
    private var mNeedLuban = false

    private val logTag = "Picker"

    /**
     * target表示最终压缩后的大小
     */
    fun setLimitImageSize(limitSize: Int, targetLimitSize:Int) : MultiPhotoPickerContractResult {
        mLimitImageSize = limitSize
        mTargetImageSize = targetLimitSize
        return this
    }

    fun setLimitVideoSize(limitSize:Long) : MultiPhotoPickerContractResult {
        mLimitVideoSize = limitSize
        return this
    }

    fun setCopyMode(copyMode: CopyMode) : MultiPhotoPickerContractResult {
        mCopyMode = copyMode
        return this
    }

    fun setCurrentMaxItems(max:Int) : MultiPhotoPickerContractResult {
        require(max > 0) {"max must > 0"}
        this.max = max
        resultContract.asOrNull<CompatMultiPickVisualMedia>()?.setCurrentMaxItems(max)
        return this
    }

    fun setNeedLubanCompress(ignoreSkb:Int = 100): MultiPhotoPickerContractResult {
        this.ignoreSizeKb = ignoreSkb
        mNeedLuban = true
        return this
    }

    @WorkerThread
    private fun lubanCompress(uriWrap: UriWrap) {
        if (!mNeedLuban || !uriWrap.isImage) {
            oneByOneCallback(uriWrap)
            return
        }

        LubanCompress()
            .setResultCallback { srcPath, resultPath, isSuc -> //主线程。Luban内部main handler回调回来的
                val path = resultPath ?: srcPath
                if (path != null) {
                    val pathFile = File(path)
                    uriWrap.uri = Uri.fromFile(pathFile)
                    uriWrap.fileSize = pathFile.length()
                    uriWrap.beLimitedSize = uriWrap.fileSize > mTargetImageSize
                    uriWrap.beCopied = true
                    val pair = MimeUtil(uriWrap.uri.getUriMimeType(null)).goodMimeTypeAndFileName()
                    uriWrap.mime = pair.first
                    uriWrap.fileName = pair.second

                    if(BuildConfig.DEBUG) Log.d(logTag, "3>luban: $uriWrap")
                }

                oneByOneCallback(uriWrap)
            }
            .compress(fragment.requireContext(), uriWrap.uri, ignoreSizeKb)
    }

    private fun ifCopy(
        uri: Uri,
        totalNum: Int,
        cr: ContentResolver
    ): UriWrap {
        val uriUtil = UriUtil(uri, cr)
        val fileSize = uri.length(cr)
        val isImage = uriUtil.isUriImage()
        val limitSize = if(isImage) mLimitImageSize.toLong() else mLimitVideoSize
        val origMimeFileName = uriUtil.goodMimeTypeAndFileName()
        val mime = origMimeFileName.first
        val fileName = origMimeFileName.second

        if (fileSize > limitSize) {
            return UriWrap(uri, totalNum, fileSize, isImage, beLimitedSize = true
                        , mime = mime, fileName = fileName)
        }

        return when (mCopyMode) {
            CopyMode.COPY_NOTHING -> {
                UriWrap(uri, totalNum, fileSize, isImage, mime = mime, fileName = fileName)
            }

            CopyMode.COPY_NOTHING_BUT_CVT_HEIC -> {
                if (uriUtil.isUriHeic()) {
                    val size = longArrayOf(-1L)
                    val copyUri = uri.copyToCacheConvert(cr, URI_COPY_PARAM_HEIC_TO_JPG, size)
                    val pair = MimeUtil(copyUri.getUriMimeType(null)).goodMimeTypeAndFileName()
                    UriWrap(copyUri, totalNum, if(size[0] == -1L) fileSize else size[0], isImage, beCopied = copyUri != uri,
                            mime = pair.first, fileName = pair.second)
                } else {
                    UriWrap(uri, totalNum, fileSize, isImage, mime = mime, fileName = fileName)
                }
            }

            CopyMode.COPY_CVT_IMAGE -> {
                if (isImage) {
                    val size = longArrayOf(-1L)
                    val copyUri = uri.copyToCacheConvert(cr, URI_COPY_PARAM_ANY_TO_JPG, size)
                    val pair = MimeUtil(copyUri.getUriMimeType(null)).goodMimeTypeAndFileName()
                    UriWrap(copyUri, totalNum, if(size[0] == -1L) fileSize else size[0], isImage = true, beCopied = copyUri != uri,
                        mime = pair.first, fileName = pair.second)
                } else {
                    UriWrap(uri, totalNum, fileSize, isImage = false, mime = mime, fileName = fileName)
                }
            }
        }
    }

    private val resultCallback:(List<@JvmSuppressWildcards Uri>)->Unit = { result->
        //自行处理不调用super
        //1. 兼容老版本的限定，选择回来多了，做下cut
        val cutUriList = if (result.size > max) { //兼容老版本无法限制picker数量
            result.subList(0, max)
        } else {
            result
        }

        if (BuildConfig.DEBUG) {
            cutUriList.forEach {
                Log.d(logTag, "1>onActivityResult: $it")
            }
        }

        fragment.lifecycleScope.launchOnThread {
            val cr = fragment.requireContext().contentResolver
            val totalNum = cutUriList.size
            cutUriList.forEach { uri->
                //2. check if copy
                val uriWrap = ifCopy(uri, totalNum, cr)
                if(BuildConfig.DEBUG) Log.d(logTag, "2>ifCopy: $uriWrap")

                if (!mNeedLuban) {
                    //3. 回调
                    fragment.lifecycleScope.launchOnUi {
                        oneByOneCallback(uriWrap)
                    }
                } else {
                    //3. luban压缩和回调
                    lubanCompress(uriWrap)
                }
            }
        }
    }

    /**
     * 推荐使用
     */
    fun launchOneByOne(type: PickerType, option: ActivityOptionsCompat?, oneByOneCallback:(UriWrap)->Unit) {
        this.oneByOneCallback = oneByOneCallback

        setResultCallback{
            resultCallback(it)
        }

        val intent = when (type) {
            PickerType.IMAGE -> PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            PickerType.IMAGE_AND_VIDEO -> PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
            PickerType.VIDEO -> PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
        }

        launcher.launch(intent, option)
    }
}