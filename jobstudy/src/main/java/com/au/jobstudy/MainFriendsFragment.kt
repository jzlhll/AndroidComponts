package com.au.jobstudy

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.au.jobstudy.databinding.FragmentMainFriendsBinding
import com.au.jobstudy.star.StarAdapter
import com.au.module_android.ui.bindings.BindingFragment

class MainFriendsFragment : BindingFragment<FragmentMainFriendsBinding>() {
    private lateinit var adapter : StarAdapter

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.rcv.layoutManager = LinearLayoutManager(binding.rcv.context)
        binding.rcv.adapter = StarAdapter().also { adapter = it }

        
    }
}