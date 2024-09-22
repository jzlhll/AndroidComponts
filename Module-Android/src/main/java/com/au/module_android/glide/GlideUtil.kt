package com.au.module_android.glide

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.widget.ImageView
import androidx.annotation.ColorInt
import com.au.module_android.Globals
import com.au.module_android.utils.deleteAll
import com.au.module_android.utils.withIoThread
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.bitmap.VideoDecoder
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

/**
 * 清除或者取消加载
 */
fun ImageView.clearByGlide() {
    Glide.with(this).clear(this)
}

/**
 * 清除磁盘缓存
 */
fun clearImageDiskCache() {
    Glide.get(Globals.app).clearDiskCache()
}

/**
 * 清除缓存大小
 */
suspend fun clearAppCacheSize() {
    withIoThread {
        clearImageDiskCache()
        Globals.app.cacheDir.deleteAll()
        Globals.app.externalCacheDir.deleteAll()
    }
}


/**
 * 第二个参数，可以对现有RequestOptions进行二次处理。
 * 去除inline避免膨胀代码。
 *
 * 内部使用
 */
fun ImageView.setImageAny(
    load: Any?,
    optionsTransform: ((RequestOptions)-> RequestOptions)? = null
) {
    load ?: return
    if (load is String && load.toString().isEmpty()) return
    //对于String的形式，会定制cache key。
    val convertLoad = if (load is String) LimitTimeGlideUrl(load) else load
    val manager = Glide.with(this)
    val opt = RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)
    val options = optionsTransform?.invoke(opt) ?: opt
    manager.load(convertLoad).apply(options).into(this)
}

fun ImageView.setImageUrlWithGrayDefault(
    load: String?,
    @ColorInt colorGray:Int? = null
) {
    val c = colorGray ?: Globals.getColor(com.au.module_androidcolor.R.color.color_glide_gray_default)
    val resInt = ColorDrawable(c)
    if (load == null) {
        setImageDrawable(resInt)
        return
    }

    setImageAny(load) {
        it.error(resInt).placeholder(resInt)
    }
}

fun ImageView.setImageUrlWithGrayError(
    load: String?,
    @ColorInt colorGray:Int? = null
) {
    val c = colorGray ?: Globals.getColor(com.au.module_androidcolor.R.color.color_glide_gray_default)
    val resInt = ColorDrawable(c)
    if (load == null) {
        setImageDrawable(resInt)
        return
    }

    setImageAny(load) {
        it.error(resInt)
    }
}

fun ImageView.setImageUrlWithResDefault(
    load: String?,
    resId: Int,
) {
    if (load == null) {
        setImageResource(resId)
        return
    }
    setImageAny(load) {
        it.error(resId).placeholder(resId)
    }
}

/**
 * 设置圆形图片
 */
fun ImageView.setImageCircleCropByGlide(load: Any?) {
    val manager = Glide.with(this)
    if (load == null || (load is String && load.isBlank())) {
        manager.clear(this)
        return
    }
    val options = RequestOptions.bitmapTransform(CircleCrop()).diskCacheStrategy(DiskCacheStrategy.ALL)
    manager.load(load).apply(options).into(this)
}

/**
 * 设置圆角图片
 */
fun ImageView.setImageRoundedCornersByGlide(
    load: Any?,
    roundingRadius: Int,
) {
    val manager = Glide.with(this)
    if (load == null || (load is String && load.isBlank())) {
        manager.clear(this)
        return
    }

    val options = RequestOptions.bitmapTransform(
        RoundedCorners(
            roundingRadius
        )
    ).diskCacheStrategy(DiskCacheStrategy.ALL)
    manager.load(load).apply(options).into(this)
}

/**
 * 加载视频第一帧
 */
fun ImageView.loadVideoFirstFrame(url: Any, sizeCall: Function3<Drawable, Int, Int, Unit>? = null) {
    val options = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        //获得第1帧图片 这里的第一个参数 以微秒为单位
        .frame(0)
        .set(VideoDecoder.FRAME_OPTION, MediaMetadataRetriever.OPTION_CLOSEST)
    Glide.with(context)
        .setDefaultRequestOptions(options)
        .load(url)
        .into(object : CustomTarget<Drawable>() {
            override fun onResourceReady(
                resource: Drawable,
                transition: Transition<in Drawable>?
            ) {
                sizeCall?.invoke(resource, resource.intrinsicWidth, resource.intrinsicHeight)
            }

            override fun onLoadCleared(placeholder: Drawable?) {

            }
        })
}

/**
 * 有的支持替换图片资源的size。比如aliyun裁剪服务等。
 */
fun resizeImgUrl(url:String?, size:String):String? {
    if (url == null) {
        return null
    }

    return url.replace(".png", "_${size}.png")
        .replace(".jpg", "_${size}.jpg").replace(".JPG", "_${size}.JPG")
        .replace(".PNG", "_${size}.PNG")
}