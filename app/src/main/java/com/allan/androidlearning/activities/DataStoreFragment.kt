package com.allan.androidlearning.activities

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.allan.androidlearning.databinding.FragmentDatastoreBinding
import com.au.module_android.click.onClick
import com.au.module_android.save.AppDataStore
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.ALog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author allan
 * @date :2023/11/7 15:33
 * @description:
 */
class DataStoreFragment : BindingFragment<FragmentDatastoreBinding>() {
    override fun afterViewCreated(savedInstanceState: Bundle?, viewBinding: FragmentDatastoreBinding) {
        viewBinding.clearBtn.onClick {
            lifecycleScope.launch(Dispatchers.Default) {
                ALog.t("clear....")
                AppDataStore.clear()
            }
        }

        viewBinding.saveBtn.onClick {
            lifecycleScope.launch {
                ALog.t("save key....")
                AppDataStore.saveSuspend("info", "abbcbdke")
                ALog.t("save key....end")
            }
        }

        viewBinding.readBtn.onClick {
            lifecycleScope.launch {
                ALog.t("read key....")
                val data = AppDataStore.read<String>("info", "default_info")
                ALog.t("read key....end $data")
            }
        }

        viewBinding.containsBtn.onClick {
            lifecycleScope.launch {
                ALog.t("contains key....")
                val isContains = AppDataStore.containsKey<String>("info")
                ALog.t("contains key....end $isContains")
            }
        }

        viewBinding.removeKeyBtn.onClick {
            ALog.t("removeKey key....")
            lifecycleScope.launch {
                ALog.t("removeKey key1....")
                val r = AppDataStore.removeSuspend<String>("info")
                ALog.t("removeKey key2....end $r")
            }
        }
    }
}