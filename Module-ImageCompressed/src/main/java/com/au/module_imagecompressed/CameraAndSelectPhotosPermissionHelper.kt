package com.au.module_imagecompressed

import androidx.fragment.app.Fragment

class CameraAndSelectPhotosPermissionHelper(val f: Fragment,
                                            supplier: CameraPermissionHelp.Supplier) {
    var pickerType = MultiPhotoPickerContractResult.PickerType.IMAGE

    val multiResult = f.compatMultiPhotoPickerForResult(3).also {
        it.setNeedLubanCompress()
    }

    fun showPhotoAndCameraDialog(maxNum:Int) {
        multiResult.setCurrentMaxItems(maxNum)
        TakePhotoActionDialog.pop(f)
    }

    val cameraHelper = CameraPermissionHelp(f, supplier)
}