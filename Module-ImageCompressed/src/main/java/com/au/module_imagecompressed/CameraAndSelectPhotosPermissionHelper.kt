package com.au.module_imagecompressed

import androidx.fragment.app.Fragment

/**
 * 为了webView的拍照，选择图片，当TakePhotoActionDialog不论是否经历过选择，拍照，或者权限失败等原因，
 * 都必须有回调，因此这里添加一个统一回调
 */
class CameraAndSelectPhotosPermissionHelper(val f: Fragment,
                                            var pickerType : MultiPhotoPickerContractResult.PickerType = MultiPhotoPickerContractResult.PickerType.IMAGE,
                                            supplier: CameraPermissionHelp.Supplier) {

    private val multiResult = f.compatMultiPhotoPickerForResult(3).also {
        it.setNeedLubanCompress()
    }
    val cameraHelper = CameraPermissionHelp(f, supplier)

    /**
     * 调用本函数，将会触发弹出界面。然后Dialog的回调会触发Fragment的onClickTakePic/onClickSelectPhoto
     *
     */
    fun showTakeActionDialog(maxNum:Int, pickerType: MultiPhotoPickerContractResult.PickerType) {
        this.pickerType = pickerType
        multiResult.setCurrentMaxItems(maxNum)
        TakePhotoActionDialog.pop(f)
    }

    fun launchSelectPhotos(callback: (Array<UriWrap>) -> Unit) {
        multiResult.launchByAll(pickerType, null, callback)
    }
}