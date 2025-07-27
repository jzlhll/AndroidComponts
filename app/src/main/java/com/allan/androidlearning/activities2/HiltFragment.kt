package com.allan.androidlearning.activities2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.ui.views.ViewFragment
import com.au.module_android.utils.logd
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@EntryFrgName(priority = 100)
@AndroidEntryPoint
class HiltFragment : ViewFragment() {

    @Inject lateinit var repo: HiltRepos

    override fun onAttach(context: Context) {
        super.onAttach(context)
        logd { "onAttach" }
    }

    override fun onUiCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        logd { "onUiCreateView" }
        repo.test()
        return LinearLayout(requireActivity())
    }

}