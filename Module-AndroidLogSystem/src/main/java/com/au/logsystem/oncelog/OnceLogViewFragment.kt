package com.au.logsystem.oncelog

import android.content.Context
import android.os.Bundle
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.au.logsystem.databinding.FragmentLogViewBinding
import com.au.module_android.click.onClick
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.FileLog
import com.au.module_android.utils.currentStatusBarAndNavBarHeight
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.myHideSystemUI
import com.au.module_android.utils.myShowSystemUI
import com.au.module_android.utils.serializableCompat
import com.au.module_android.utils.unsafeLazy
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
    }

    private val mAdapter by unsafeLazy {
        LogViewAdapter(binding.rcv).apply {
            loadMoreAction = {
                loadMore()
            }
        }
    }

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

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        requireActivity().myHideSystemUI()

        mRcv = binding.rcv.also {
            it.adapter = mAdapter
            it.layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            it.itemAnimator = null

            onceRead()
        }

        binding.floatBtn.onClick {
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
    }

    private fun onceRead() {
        FileLog.ignoreWrite = true
        lifecycleScope.launchOnThread {
            val beans = logViewReader?.onceRead() ?: emptyList()
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