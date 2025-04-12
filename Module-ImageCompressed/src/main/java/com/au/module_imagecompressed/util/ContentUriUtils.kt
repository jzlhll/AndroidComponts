package com.au.module_imagecompressed.util

import android.content.ContentResolver
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.WorkerThread
import androidx.core.net.toFile
import com.au.module_android.Globals
import com.au.module_android.utils.logt
import com.au.module_imagecompressed.CropCircleImageFragment.Companion.DIR_CROP
import com.au.module_imagecompressed.util.getUriMimeType
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

private const val SUB_CACHE_DIR = "luban_disk_cache"
private const val COPY_FILE_PREFIX = "copy_"

fun clearLubanAndCropCache(clearLubanCompress:Boolean = true, clearUCrop:Boolean = true) {
    if (clearLubanCompress) {
        try {
            val cmpImagesPath = File(Globals.app.cacheDir.absolutePath + "/$SUB_CACHE_DIR")
            cmpImagesPath.listFiles()?.forEach {
                it.delete()
            }
        } catch (e:Exception) {
            e.printStackTrace()
        }

        try {
            val cmpImagesPath = File(Globals.app.externalCacheDir?.absolutePath + "/$SUB_CACHE_DIR")
            cmpImagesPath.listFiles()?.forEach {
                it.delete()
            }
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }

    if (clearUCrop) {
        try {
            val cmpImagesPath = File(Globals.app.cacheDir.absolutePath + "/$DIR_CROP")
            cmpImagesPath.listFiles()?.forEach {
                it.delete()
            }
        } catch (e:Exception) {
            e.printStackTrace()
        }

        try {
            val cmpImagesPath = File(Globals.app.externalCacheDir?.absolutePath + "/$DIR_CROP")
            cmpImagesPath.listFiles()?.forEach {
                it.delete()
            }
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }
}

fun isUrlHasImage(url: String): Boolean {
    val lowUrl = url.lowercase()
    return (lowUrl.endsWith(".jpg")
            || lowUrl.endsWith(".jpeg")
            || lowUrl.endsWith(".png")
            || lowUrl.endsWith(".heic"))
}

fun isHasHttp(path: String): Boolean {
    if (TextUtils.isEmpty(path)) {
        return false
    }
    return path.startsWith("http") || path.startsWith("https")
}

fun isPicNeedCompress(path:String) = isUrlHasImage(path) && !isHasHttp(path)

/**
 * 将Uri识别，拷贝到本地cache；如果param有传参，则会进行转换拷贝。
 *
 * 不管是不是图片是不是进行转换，都会拷贝（file型的uri除外）。
 *
 * 本函数会耗时。自行放到scope中运行。
 */
@WorkerThread
fun Uri.copyToCacheConvert(cr:ContentResolver, param:String? = URI_COPY_PARAM_HEIC_TO_JPG, size:LongArray? = null) : Uri{
    val file = this.copyToCacheFile(cr, param, size)
    return if (file == null) {
        this //被limit或者size为0
    } else {
        Uri.fromFile(file)
    }
}

/**
 * 将List<Uri>遍历识别，全部拷贝到本地cache；如果param有传参，则会进行转换拷贝。
 *
 * 不管是不是图片是不是进行转换，都会拷贝（file型的uri除外）。
 *
 * 本函数会耗时。自行放到scope中运行。
 */
@WorkerThread
fun List<Uri>.copyToCacheConvert(cr:ContentResolver, param:String? = URI_COPY_PARAM_HEIC_TO_JPG) : List<Uri> {
    return this.map { uri-> uri.copyToCacheConvert(cr, param) }
}

const val URI_COPY_PARAM_HEIC_TO_JPG = "only_heic_convert_to_jpg"
const val URI_COPY_PARAM_HEIC_TO_PNG = "only_heic_convert_to_png"
const val URI_COPY_PARAM_ANY_TO_JPG = "any_convert_to_jpg"

private fun isSupportConvertImage(extension: String): Boolean {
    val imageExtensions = listOf("jpg", "jpeg", "png", "heic")
    return extension in imageExtensions
}

fun Uri.copyToCacheFileSchemeFile(size:LongArray? = null): File? {
    val file = this.path?.let { File(it) }
    if (file != null) {
        size?.set(0, file.length())
    }
    return file
}

fun Uri.copyToCacheFileSchemeContent(cr: ContentResolver, param:String? = null, size:LongArray? = null) : File? {
    val cacheDir = Globals.goodCacheDir
    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(getUriMimeType(cr))?.lowercase()
    val isSourceHeic = extension == "heic"
    logt(tag = "picker") { "$this $param, extension: $extension"}
    var cvtExtension = extension
    if (extension != null && isSupportConvertImage(extension)) {
        when (param) {
            URI_COPY_PARAM_ANY_TO_JPG -> {
                cvtExtension = "jpg"
            }

            URI_COPY_PARAM_HEIC_TO_PNG -> {
                if (isSourceHeic) {
                    cvtExtension = "png"
                }
            }

            URI_COPY_PARAM_HEIC_TO_JPG -> {
                if (isSourceHeic) {
                    cvtExtension = "jpg"
                }
            }
        }
    }

    val displayName = COPY_FILE_PREFIX + System.currentTimeMillis() + "_" + (Math.random() * 1000).toInt().toString() + "." + cvtExtension
    val subDirFile = File(cacheDir.absolutePath + "/$SUB_CACHE_DIR")
    if (!subDirFile.exists()) {
        subDirFile.mkdirs()
    }
    val cache = File(cacheDir.absolutePath + "/$SUB_CACHE_DIR", displayName)
    val file = cache
    copyFromCr(cr, cache, param, extension, isSourceHeic, size)
    return file
}

private fun Uri.copyFromCr(
    cr: ContentResolver,
    cache: File,
    param: String?,
    extension: String?,
    isSourceHeic: Boolean,
    size: LongArray?
) {
    try {
        cr.openInputStream(this)?.use { inputStream ->
            val fos = FileOutputStream(cache)
            var cvtFmt: String? = null
            if (param != null && extension != null && isSupportConvertImage(extension)) {
                when (param) {
                    URI_COPY_PARAM_ANY_TO_JPG -> {
                        if (extension != "jpg" && extension != "jpeg") {
                            cvtFmt = "jpg"
                        }
                    }

                    URI_COPY_PARAM_HEIC_TO_JPG -> {
                        if (isSourceHeic) {
                            cvtFmt = "jpg"
                        }
                    }

                    URI_COPY_PARAM_HEIC_TO_PNG -> {
                        if (isSourceHeic) {
                            cvtFmt = "png"
                        }
                    }
                }
            }

            if (cvtFmt != null) {
                copyImageAndCvtTo(inputStream, fos, cvtFmt)
            } else {
                copyFile(inputStream, fos)
            }

            fos.flush()
            fos.close()

            size?.set(0, cache.length())
            logt(tag = "picker") { "$cache $cvtFmt after copy ${size?.get(0)}" }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * 经过研究，android对于content uri想要使用File最好的办法，就是拷贝到自己的目录下。
 * 才是最保险的，而且不需要考虑权限问题。
 *
 * 不管是不是图片是不是进行转换，都会拷贝（file型的uri除外）。
 *
 * 自行考虑放到Scope中运行。可能会耗时比较多，比如拷贝视频。
 *
 * @param param 参考URI_COPY_PARAM_XXX
 *
 * @return 不太可能是空。
 */
@WorkerThread
fun Uri.copyToCacheFile(cr: ContentResolver, param:String? = null, size:LongArray? = null): File? {
    var file: File? = null
    if (this.scheme == ContentResolver.SCHEME_FILE) {
        file = copyToCacheFileSchemeFile(size)
    } else if (this.scheme == ContentResolver.SCHEME_CONTENT) {
        file = copyToCacheFileSchemeContent(cr, param, size)
    }
    return file
}

@Throws(IOException::class)
fun copyFile(
    inputStream: InputStream,
    out: OutputStream
): Long {
    var progress: Long = 0
    val buffer = ByteArray(8192)

    var t: Int
    while ((inputStream.read(buffer).also { t = it }) != -1) {
        out.write(buffer, 0, t)
        progress += t.toLong()
    }
    return progress
}

//将图片转码
@Throws(Exception::class)
fun copyImageAndCvtTo(inputStream: InputStream, outputStream: FileOutputStream, fmt:String) {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = false
    val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
    bitmap?.compress(if(fmt == "png") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG, 100, outputStream)
}

/**
 * 获取content uri的mimeType
 */
fun Uri.getUriMimeType(cr: ContentResolver?) : String {
    if (scheme == "file") {
        val extension = this.toFile().extension.lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
    } else {
        require(cr != null)
        var mimeType = cr.getType(this)
        if (mimeType == null) {
            // 如果无法直接获取MIME类型，则从Uri中读取
            cr.query(this, null, null, null, null)?.use { cursor->
                val mimeTypeIndex = cursor.getColumnIndex("mime_type")
                if (mimeTypeIndex != -1 && cursor.moveToFirst()) {
                    mimeType = cursor.getString(mimeTypeIndex)
                }
            }
        }
        return mimeType ?: "*/*"
    }
}

/**
 * 获取Uri的文件大小
 */
fun Uri.length(cr: ContentResolver, schemeForce:String? = null) : Long {
    var resultLength = -1L
    when (schemeForce ?: scheme) {
        ContentResolver.SCHEME_FILE -> {
            // Try to get content length from content scheme uri or file scheme uri
            var fileDescriptor: ParcelFileDescriptor? = null
            try {
                fileDescriptor = cr.openFileDescriptor(this, "r")
                    ?: throw Exception("Content provider recently crashed")
                resultLength = fileDescriptor.statSize
            } catch (e: Exception) {
                Log.d("allan", e.message ?: e.javaClass.simpleName)
                resultLength = -1L
            } finally {
                fileDescriptor?.close()
            }
        }

        ContentResolver.SCHEME_CONTENT -> {
            // Try to get content length from the content provider column OpenableColumns.SIZE
            // which is recommended to implement by all the content providers
            var cursor: Cursor? = null
            try {
                cursor = cr.query(
                    this,
                    arrayOf(OpenableColumns.SIZE),
                    null,
                    null,
                    null
                ) ?: throw Exception("Content provider returned null or crashed")
                val sizeColumnIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeColumnIndex != -1 && cursor.count > 0) {
                    cursor.moveToFirst()
                    resultLength = cursor.getLong(sizeColumnIndex)
                } else {
                    resultLength = -1L
                }
            } catch (e: Exception) {
                Log.d("allan", e.message ?: e.javaClass.simpleName)
                resultLength = -1L
            } finally {
                cursor?.close()
            }

            if (resultLength == -1L) {
                resultLength = this.length(cr, ContentResolver.SCHEME_FILE)
            }

            if (resultLength == -1L) {
                cr.openInputStream(this)?.use {
                    resultLength = it.available().toLong()
                }
            }
        }
        ContentResolver.SCHEME_ANDROID_RESOURCE -> {
            // Try to get content length from content scheme uri, file scheme uri or android resource scheme uri
            var assetFileDescriptor: AssetFileDescriptor? = null
            try {
                assetFileDescriptor = cr.openAssetFileDescriptor(this, "r")
                    ?: throw Exception("Content provider recently crashed")
                resultLength = assetFileDescriptor.length
            } catch (e: Exception) {
                Log.d("allan", e.message ?: e.javaClass.simpleName)
                resultLength = -1L
            } finally {
                assetFileDescriptor?.close()
            }
        }
    }

    return resultLength
}

//
//fun getVideoThumbnail(cr:ContentResolver, file: File, width:Int, height:Int): Bitmap? {
//    return getVideoThumbnail(cr, Uri.fromFile(file), width, height)
//}
//
//fun getThumbnail(cr:ContentResolver, contentUri: Uri, width:Int, height:Int): Bitmap? {
//    try {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            cr.loadThumbnail(contentUri, Size(width, height), null)
//        } else {
//            val path = contentUri.path
//            if (path == null) {
//                null
//            } else {
//                ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND)
//            }
//        }
//    } catch (e:Exception) {
//        e.printStackTrace()
//    }
//    return null
//}
//
//@Throws(IOException::class)
//fun createThumbnailFromImageInputStream(inputStream: InputStream?, width: Int, height: Int): Bitmap? {
//    val options = BitmapFactory.Options()
//    options.inJustDecodeBounds = true
//    // Calculate inSampleSize
//    options.inSampleSize = calculateInSampleSize(options, width, height)
//
//    // Decode bitmap with inSampleSize set
//    options.inJustDecodeBounds = false
//    return BitmapFactory.decodeStream(inputStream, null, options)
//}
//
//private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
//    // Raw height and width of image
//    val height = options.outHeight
//    val width = options.outWidth
//    var inSampleSize = 1
//
//    if (height > reqHeight || width > reqWidth) {
//        val halfHeight = height / 2
//        val halfWidth = width / 2
//
//        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
//        // height and width larger than the requested height and width.
//        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
//            inSampleSize *= 2
//        }
//    }
//
//    return inSampleSize
//}

