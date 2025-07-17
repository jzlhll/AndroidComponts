package com.au.module_imagecompressed

import androidx.activity.result.ActivityResultCallback
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.au.module_android.Globals
import com.au.module_android.utils.ignoreError
import com.au.module_android.utils.logd
import com.au.module_android.utilsmedia.UriHelper
import com.au.module_androidui.dialogs.TakePhotoActionDialog
import java.io.File

/**
 * @author allan
 * @date :2024/12/5 10:01
 * @description: 目前支持拍照和选择视频或者图片。
 */
class TakeAndSelectMediaPermissionHelper(val f:Fragment,
                                         val applicationId:String,
                                         val picMaxSize:Int = 3,
                                         val maxPicFileLength:Int = 5 * 1024 * 1024,
                                         val maxVideoFileLength:Long = 100 * 1024 * 1024,
                                         var pickerType:MultiPhotoPickerContractResult.PickerType
                                            = MultiPhotoPickerContractResult.PickerType.IMAGE,)
    : TakePhotoActionDialog.ITakePhotoActionDialogCallback{
    private val cameraHelper = CameraPermissionHelp(f)
    private val multiPickerForResult = f.compatMultiPhotoPickerForResult(picMaxSize).apply {
        setNeedLubanCompress()
        setLimitImageSize(maxPicFileLength * 10, maxPicFileLength) //压缩前给大一些
        setLimitVideoSize(maxVideoFileLength)
    }

    var oneByOneResultAction:((UriWrap)->Unit)? = null
    var allResultsAction:((Array<UriWrap>)->Unit)? = null

    fun changeMultiPickerForResultMaxNum(maxNum:Int) {
        multiPickerForResult.setCurrentMaxItems(maxNum)
    }

    fun showPhotoAndCameraDialog() {
        TakePhotoActionDialog.pop(f)
    }

    override fun onClickTakePic() {
        cameraHelper.safeRunTakePic(offerBlock = {
            val picture = File(Globals.goodCacheDir.path)
            picture.mkdirs()
            val file = File(picture, "pic_" + System.currentTimeMillis() + ".jpg")
            val uri = FileProvider.getUriForFile(
                Globals.app,
                "${applicationId}.fileprovider",
                file
            )

            Pair(uri, object:ActivityResultCallback<Boolean> {
                override fun onActivityResult(result: Boolean) {
                    if (result) {
                        LubanCompress().setResultCallback { srcPath, resultPath, isSuc ->
                            val r = if(isSuc) resultPath else srcPath
                            ignoreError {
                                val resultFile = File(r)
                                val resultUri = resultFile.toUri()
                                val cvtUri = UriHelper(resultUri, Globals.app.contentResolver).imageFileConvertToUriWrap()
                                logd { "cvtUri $cvtUri" }
                            }
                        }.compress(f.requireContext(), file.toUri()) //必须是file的scheme。那个FileProvider提供的则不行。
                    }
                }
            })
        })

    }

    override fun onClickSelectPhoto() {
        if (allResultsAction != null) {
            multiPickerForResult.launchByAll(pickerType, null) { uriWraps->
                val noLimitedUriWraps = uriWraps.filter { !it.beLimitedSize }
                if (noLimitedUriWraps.size != uriWraps.size) {
                    //todo toast
                }
                allResultsAction?.invoke(noLimitedUriWraps.toTypedArray())
            }
        } else {
            multiPickerForResult.launchOneByOne(pickerType, null) {
                    uriWrap ->
                if (uriWrap.isImage && uriWrap.fileSize > maxPicFileLength) {
                    //f.toast(getStringCompat(R.string.pic_too_lager))
                    return@launchOneByOne
                }

                if (!uriWrap.isImage && uriWrap.fileSize > maxVideoFileLength) {
                    //f.toast(getStringCompat(R.string.vid_too_lager)) //todo toast
                    return@launchOneByOne
                }

                oneByOneResultAction?.invoke(uriWrap)
            }
        }
    }

    override fun onNothingClosed() {
        //do nothing.
    }
}