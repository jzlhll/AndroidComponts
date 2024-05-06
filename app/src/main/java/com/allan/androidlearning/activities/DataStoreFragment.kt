package com.allan.androidlearning.activities

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.allan.androidlearning.databinding.FragmentDatastoreBinding
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.ALog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author au
 * @date :2023/11/7 15:33
 * @description:
 */
class DataStoreFragment : BindingFragment<FragmentDatastoreBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewBinding = binding
        viewBinding.clearBtn.onClick {
            lifecycleScope.launch(Dispatchers.Default) {
                ALog.t("clear....")
                com.au.module.cached.AppDataStore.clear()
            }
        }

        viewBinding.saveBtn.onClick {
            com.au.module.cached.AppDataStore.save("info", "abbcbdke")
        }

        viewBinding.readBtn.onClick {
            lifecycleScope.launch {
                ALog.t("read key....")
                val data = com.au.module.cached.AppDataStore.read<String>("info", "default_info")
                ALog.t("read key....end $data")
            }
        }

        viewBinding.containsBtn.onClick {
            lifecycleScope.launch {
                ALog.t("contains key....")
                val isContains = com.au.module.cached.AppDataStore.containsKey<String>("info")
                ALog.t("contains key....end $isContains")
            }
        }

        viewBinding.removeKeyBtn.onClick {
            ALog.t("removeKey key....")
            lifecycleScope.launch {
                ALog.t("removeKey key1....")
                val r = com.au.module.cached.AppDataStore.removeSuspend<String>("info")
                ALog.t("removeKey key2....end $r")
            }
        }
    }
}