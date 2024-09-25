package com.allan.androidlearning.activities

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.allan.androidlearning.databinding.FragmentDatastoreBinding
import com.allan.classnameanno.EntroFrgName
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.logt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author au
 * @date :2023/11/7 15:33
 * @description:
 */
@EntroFrgName
class DataStoreFragment : BindingFragment<FragmentDatastoreBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewBinding = binding
        viewBinding.clearBtn.onClick {
            lifecycleScope.launch(Dispatchers.Default) {
                logt { "clear...." }
                com.au.module_cached.AppDataStore.clear()
            }
        }

        viewBinding.saveBtn.onClick {
            com.au.module_cached.AppDataStore.save("info", "abbcbdke")
        }

        viewBinding.readBtn.onClick {
            lifecycleScope.launch {
                val data = com.au.module_cached.AppDataStore.read<String>("info", "default_info")
            }
        }

        viewBinding.containsBtn.onClick {
            lifecycleScope.launch {
                val isContains = com.au.module_cached.AppDataStore.containsKey<String>("info")
            }
        }

        viewBinding.removeKeyBtn.onClick {
            lifecycleScope.launch {
                val r = com.au.module_cached.AppDataStore.removeSuspend<String>("info")
            }
        }
    }
}