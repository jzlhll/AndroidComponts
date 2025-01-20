package com.allan.androidlearning.activities2

import android.os.Bundle
import com.allan.androidlearning.databinding.FragmentRmwlPropertyBinding
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_cached.delegate.AppDataStoreLongCache
import com.au.module_cached.delegate.SharedPrefStringCache

@EntryFrgName
class RMWLPropertyFragment : BindingFragment<FragmentRmwlPropertyBinding>() {
    private var sharedStr by SharedPrefStringCache("sp_cache_myname", "")
    private var dataStoreLong by AppDataStoreLongCache("datastore_cache_myname", 0L)

    private var mLog = ""

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.saveBtn.onClick {
            sharedStr = "this a newName " + System.currentTimeMillis()
        }
        binding.saveBtn2.onClick {
            dataStoreLong = System.currentTimeMillis()
        }

        binding.readBtn.onClick {
            mLog += "\n" + sharedStr
            binding.tv.text = mLog
        }
        binding.readBtn2.onClick {
            mLog += "\n" + dataStoreLong
            binding.tv.text = mLog
        }
    }
}