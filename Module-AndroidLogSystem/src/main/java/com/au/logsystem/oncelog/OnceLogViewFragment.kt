package com.au.logsystem.oncelog

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.au.logsystem.R
import com.au.logsystem.databinding.FragmentLogViewBinding
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.FileLog
import com.au.module_android.utils.currentStatusBarAndNavBarHeight
import com.au.module_android.utils.dp
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.myHideSystemUI
import com.au.module_android.utils.myShowSystemUI
import com.au.module_android.utils.serializableCompat
import com.au.module_android.utils.unsafeLazy
import com.au.module_cached.delegate.AppDataStoreJsonCache
import java.io.File

class OnceLogViewFragment : BindingFragment<FragmentLogViewBinding>() {
    companion object {
        fun show(context: Context, file: File) {
            FragmentShellActivity.start(
                context,
                OnceLogViewFragment::class.java,
                Bundle().apply {
                    putSerializable("file", file)
                })
        }

        private const val TAG_FULL_SCREEN = "tag_fullScreen"
        private const val TAG_THREAD_PROCESS = "tag_threadProcess"
        private const val TAG_LEVEL = "tag_threadProcess"
        private const val TAG_TAG = "tag_tag"

        private const val TAG_IS_WRAP = "tag_isWrap"
    }

    private val mAdapter by unsafeLazy {
        LogViewAdapter(binding.rcv).apply {
            loadMoreAction = {
                loadMore()
            }
        }
    }

    private var mShowInfo by AppDataStoreJsonCache("logSysShownInfo",
        LogViewShownInfo(isWrap = false, fullScreen = true, time=true, threadProcess=true, level=true, tag=true),
        LogViewShownInfo::class.java)

    private fun loadMore() {
        mRcv.post {
            mAdapter.loadNext()
        }
    }

    private lateinit var mRcv: RecyclerView

    private val logViewReader: OnceLogViewReader? by unsafeLazy {
        val file: File? = arguments?.serializableCompat("file")
        if (file == null || !file.exists()) {
            null
        } else {
            logdNoFile { "logView: $file length: ${file.length()}" }
            OnceLogViewReader(file)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        FileLog.ignoreWrite = false
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        requireActivity().myHideSystemUI()
        FileLog.ignoreWrite = true

        mRcv = binding.rcv.also {
            it.adapter = mAdapter
            it.layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            it.itemAnimator = null

            onceRead()
        }

        initFloatingButtons()
    }

    private fun initFloatingButtons() {
        val f = binding.floatBtn
        //第一个是菜单图标  第二个是菜单背景颜色  第三个是点击回调
        val bgColor = resources.getColor(com.au.module_androidcolor.R.color.colorPrimary_press, null)
        f.addElementOnCreate(TAG_FULL_SCREEN, R.drawable.ic_floating_full_screen, bgColor) {
            if (currentIsFull) {
                requireActivity().myShowSystemUI()
                binding.floatBtn.post {
                    val barsHeights = requireActivity().currentStatusBarAndNavBarHeight()!!
                    binding.root.updatePadding(top = barsHeights.first, bottom = barsHeights.second)
                    currentIsFull = false
                }
            } else {
                requireActivity().myHideSystemUI()
                binding.floatBtn.post {
                    binding.root.updatePadding(top = 0, bottom = 0)
                    currentIsFull = true
                }
            }
        }
        f.addElementOnCreate(TAG_TAG, R.drawable.ic_floating_tag, bgColor) {
            mShowInfo = mShowInfo.also { it.tag = !it.tag }
            changeShown()
        }
        f.addElementOnCreate(TAG_THREAD_PROCESS, R.drawable.ic_floating_thread_process, bgColor) {
            mShowInfo = mShowInfo.also { it.threadProcess = !it.threadProcess }
            changeShown()
        }
        f.addElementOnCreate(TAG_LEVEL, R.drawable.ic_floating_level, bgColor) {
            mShowInfo = mShowInfo.also { it.level = !it.level }
            changeShown()
        }
        f.addElementOnCreate(TAG_IS_WRAP, R.drawable.ic_floating_wrap_no, bgColor) {
            mShowInfo = mShowInfo.also { it.isWrap = !it.isWrap }
            changeShown()
        }

        f.setMyAngle(90) //这个是展开的总角度  建议取90的倍数
        f.setMyScale(0.88f) //设置弹出缩放的比例  1为不缩放 范围是0—1
        f.setMyLength(125.dp) //设置弹出的距离
    }

    private fun changeShown() {
        mAdapter.datas.forEach {
            it.showBits = mShowInfo
        }
        mAdapter.notifyDataSetChanged()
    }

    private fun onceRead() {
        FileLog.ignoreWrite = true
        lifecycleScope.launchOnThread {
            val beans = logViewReader?.onceRead(mShowInfo) ?: emptyList()
            mRcv.post {
                FileLog.ignoreWrite = false
                mAdapter.initBy(beans)
            }
        }
    }

    private var currentIsFull = true

    override fun isPaddingStatusBar(): Boolean {
        return false
    }

    override fun isPaddingNavBar(): Boolean {
        return false
    }
}