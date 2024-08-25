package com.au.module_android.glide

import android.content.Context
import android.util.Log
import com.au.module.android.BuildConfig
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.Excludes
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpLibraryGlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import okhttp3.OkHttpClient
import java.io.InputStream

@GlideModule
@Excludes(OkHttpLibraryGlideModule::class)
class OkHttpGlideModule : AppGlideModule() {
    private val noSslCheck = true

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.apply {
            //设置Bitmap的缓存池
            //setBitmapPool(LruBitmapPool(30))
            //设置内存缓存
            //val maxMemory = Runtime.getRuntime().maxMemory().toInt()
            //val cacheSize = maxMemory / 8
            //setMemoryCache(LruResourceCache(cacheSize.toLong()))
            //设置磁盘缓存
            setDiskCache(InternalCacheDiskCacheFactory(context, "imageCache", 1024 * 1024 * 32))
            //设置日志级别
            if (BuildConfig.DEBUG) {
                setLogLevel(Log.DEBUG)
            } else {
                setLogLevel(Log.WARN)
            }
            //设置全局选项
            setDefaultRequestOptions(RequestOptions().format(DecodeFormat.PREFER_ARGB_8888))
        }
    }

    override fun isManifestParsingEnabled(): Boolean {
        return false
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        //添加okhttp的使用。
        val builder = OkHttpClient.Builder()
        if (noSslCheck) {
            val trustAllCerts = TrustAllCerts()
            builder
                .sslSocketFactory(trustAllCerts.createSSLSocketFactory(), trustAllCerts)
                .hostnameVerifier(TrustAllCerts.TrustAllHostnameVerifier())
        }
        val okHttpClient = builder.build()
        registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(okHttpClient))
    }
}