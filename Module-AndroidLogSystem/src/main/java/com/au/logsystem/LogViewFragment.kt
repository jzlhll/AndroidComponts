package com.au.logsystem

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.ui.views.ViewFragment
import com.au.module_android.utils.FileLog
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.serializableCompat
import com.au.module_android.utils.unsafeLazy
import kotlinx.coroutines.delay
import java.io.File
import kotlin.math.min


class LogViewFragment : ViewFragment() {
    companion object {
        fun show(context: Context, file: File) {
            FragmentShellActivity.start(context,
                LogViewFragment::class.java,
                Bundle().apply {
                    putSerializable("file", file)
                })
        }
    }

    private val mAdapter = LogViewAdapter().apply {
        loadMoreAction = {
            if (mAdapterSubmitCount > 0) {
                mRcv.post {
                    loadNext()
                }
            }
        }
    }
    private var mAdapterSubmitCount = 0
    private var mBeanIndex = 0
    private var mLoadedToRcvIndex = 0

    private lateinit var mRcv: RecyclerView

    private val logViewReader: OnceLogViewReader? by unsafeLazy {
        val file:File? = arguments?.serializableCompat("file")
        if (file == null || !file.exists()) {
            null
        } else {
            logdNoFile { "logView: $file length: ${file.length()}" }
            OnceLogViewReader(file)
        }
    }

    private var loadedBeans: List<LogViewNormalBean>?= null

    private fun onceRead() {
        logdNoFile { "logView: onceRead" }
        logViewReader?.let { reader->
            lifecycleScope.launchOnThread {
                val lines = reader.readAll()
                delay(20)
                logdNoFile { "logView: lines ${lines.size}" }
                val beans = lines.chunked(5) { chunk ->
                    chunk.joinToString(separator = "\n")
                }.map {
                    LogViewNormalBean(mBeanIndex++, it)
                }
                logdNoFile { "logView: beans ${beans.size}" }

                loadedBeans = beans

                FileLog.ignoreWrite = false
                mRcv.post {
                    loadNext()
                }
            }
        }
    }

    private fun loadNext() {
        val beans = loadedBeans ?: return
        if (beans.isEmpty()) return
        if (!mRcv.isAttachedToWindow) {
            return
        }

        val lastIndex = mLoadedToRcvIndex
        mLoadedToRcvIndex = min(beans.size - 1, mLoadedToRcvIndex + 100)
        val hasMore = mLoadedToRcvIndex + 1 < beans.size
        val subList = beans.subList(lastIndex, mLoadedToRcvIndex + 1)

        if (!mRcv.isAttachedToWindow) {
            return
        }

        if (mAdapterSubmitCount == 0) {
            mAdapter.initDatas(subList, hasMore)
        } else {
            mAdapter.appendDatas(subList, hasMore)
        }
        mAdapterSubmitCount++
    }

    override fun onUiCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        FileLog.ignoreWrite = true

        return RecyclerView(inflater.context).apply {
            mRcv = this
            adapter = mAdapter
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            itemAnimator = null

            onceRead()
        }
    }
}