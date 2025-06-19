package com.au.module_imagecompressed

import androidx.fragment.app.Fragment
import com.au.module_android.permissions.systemTakePictureForResult

/**
 * @author allan
 * @date :2024/12/5 10:01
 * @description: 目前支持拍照和选择视频或者图片。
 */
class TakeAndSelectMediaPermissionHelper(val f:Fragment,
                                         val picMaxSize:Int = 3,
                                         val maxPicFileLength:Int = 200 * 1024 * 1024,
                                         val maxVideoFileLength:Long = 8 * 1024 * 1024 * 1024) {
    private val cameraHelper = CameraPermissionHelp(f)
    private val sysTakePicResult = f.systemTakePictureForResult()
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
//
//    fun onClickTakePic() {//videoShare 是filePaths.xml定义的。用于给外部应用（这里是拍照程序）共享。
//        cameraHelper.safeRun(doBlock = {
//            val picture = File(BaseGlobalConst.goodCacheDir.path + "/videoShare")
//            picture.mkdirs()
//            val file = File(picture, "pic_" + System.currentTimeMillis() + ".jpg")
//            val uri = FileProvider.getUriForFile(
//                BaseGlobalConst.app,
//                "${BuildConfig.APPLICATION_ID}.fileprovider",
//                file
//            )
//            val fileUri = file.toUri()
//
//            sysTakePicResult.launch(uri, null) { suc->
//                if (suc) {
//                    LubanCompress().setResultCallback { srcPath, resultPath, isSuc ->
//                        val r = if(isSuc) resultPath else srcPath
//                        ignoreError {
//                            val resultFile = File(r)
//                            val resultUri = resultFile.toUri()
//                            val uriWrap = UriUtil(resultUri, BaseGlobalConst.app.contentResolver).myFileConvertToUriWrap()
//                            if (uriWrap.fileSize > maxPicFileLength) {
//                                f.toast(getStringCompat(R.string.pic_too_lager))
//                            } else {
//                                if(allResultsAction != null) allResultsAction?.invoke(arrayOf(uriWrap)) else oneByOneResultAction?.invoke(uriWrap)
//                            }
//                        }
//                    }.compress(f.requireContext(), fileUri) //必须是file的scheme。那个FileProvider提供的则不行。
//                } else {
//                    if (allResultsAction != null) {
//                        allResultsAction?.invoke(arrayOf()) //没拍照也要回调。
//                    }
//                }
//            }
//        })
//    }

    fun onClickSelectVideo() {
        val pickerType = MultiPhotoPickerContractResult.PickerType.VIDEO
        onClickSelectPhoto(pickerType)
    }

    fun onClickSelectPhotoAndVideo() {
        onClickSelectPhoto(MultiPhotoPickerContractResult.PickerType.IMAGE_AND_VIDEO)
    }

    fun onClickSelectPhoto(pickerType: MultiPhotoPickerContractResult.PickerType = MultiPhotoPickerContractResult.PickerType.IMAGE) {
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
                    return@launchOneByOne
                }

                if (!uriWrap.isImage && uriWrap.fileSize > maxVideoFileLength) {
                    return@launchOneByOne
                }

                oneByOneResultAction?.invoke(uriWrap)
            }
        }
    }
}