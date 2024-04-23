package com.allan.autoclickfloat.activities.recordprojects

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.allan.autoclickfloat.databinding.RecordProjectsAllFragmentBinding
import com.au.module_android.datastore.AppDataStore
import com.au.module_android.ui.bindings.BindingFragment
import kotlinx.coroutines.launch

/**
 * @author allan
 * @date :2024/4/23 17:46
 * @description:
 */
class RecordProjectsAllFragment : BindingFragment<RecordProjectsAllFragmentBinding>() {
    private val adapter = RecordProjectsAllAdapter()

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.rcv.adapter = adapter
        binding.rcv.layoutManager = LinearLayoutManager(binding.rcv.context)

        lifecycleScope.launch {
            AppDataStore.read()
        }

        adapter.submitList()
    }
}